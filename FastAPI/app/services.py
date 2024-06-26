# services.py
from sqlalchemy.future import select
from sqlalchemy.orm import sessionmaker
from sqlalchemy.ext.asyncio import create_async_engine, AsyncSession
from sklearn.feature_extraction.text import TfidfVectorizer
from fastapi import HTTPException
from contextlib import asynccontextmanager
from .models import Animal
from pymilvus import connections, FieldSchema, CollectionSchema, DataType, Collection
import numpy as np  

# Milvus에 연결
connections.connect("default", host="localhost", port="19530")

# 컬렉션 스키마 정의
fields = [
    FieldSchema(name="id", dtype=DataType.INT64, is_primary=True, auto_id=False),
    FieldSchema(name="vector", dtype=DataType.FLOAT_VECTOR, dim=512)  # 벡터의 차원 설정
]

animal_schema = CollectionSchema(fields, "animal_vectors")

# 컬렉션 생성
animal_collection = Collection("animal_collection", animal_schema)

# MySQL스 설정
DATABASE_URL = "mysql+aiomysql://root:0623@localhost/ForPaw"
engine = create_async_engine(DATABASE_URL)
AsyncSessionLocal = sessionmaker(autocommit=False, autoflush=False, bind=engine, class_=AsyncSession)

@asynccontextmanager
async def get_db_session():
    async with AsyncSessionLocal() as session:
        yield session

# 벡터화 객체 초기화
vectorizer = TfidfVectorizer()

async def load_and_vectorize_data():
    async with get_db_session() as db:
        result = await db.execute(select(Animal))
        animals = result.scalars().all()
        texts = [
            f"{animal.shelter_id} {animal.age} {animal.color} {animal.gender} {animal.kind} {animal.region} {animal.special_mark} {animal.happen_place}"
            for animal in animals
        ]

    tfidf_matrix = vectorizer.fit_transform(texts)
    vectors = tfidf_matrix.toarray()

    ids = [animal.id for animal in animals]

    # Milvus에 데이터 삽입
    animal_collection.insert([ids, vectors])
    
    return vectors, {animal.id: idx for idx, animal in enumerate(animals)}

async def get_similar_animals(animal_id, animal_index, tfidf_matrix, num_results=5):
    async with get_db_session() as db:
        result = await db.execute(select(Animal).filter(Animal.id == animal_id))
        query_animal = result.scalars().first()
        if not query_animal:
            raise HTTPException(status_code=404, detail="Animal not found")
    
        # 조회된 동물의 인덱스 찾기
        idx = animal_index.get(animal_id)
        if idx is None:
            raise HTTPException(status_code=404, detail="Index not found for the given animal ID")
    
        # 해당 동물의 벡터를 배열로 변환하고, 전체 데이터베이스 벡터와의 코사인 유사도 계산
        query_vec = tfidf_matrix[idx].toarray().tolist()
        search_params = {"metric_type": "L2", "params": {"nprobe": 10}}
        results = animal_collection.search([query_vec], "vector", search_params, limit=num_results)
        
        similar_animal_ids = [res.id for res in results[0].ids]

        return similar_animal_ids
    
# 주기적으로 새로운 동물 데이터를 업데이트하는 함수
async def update_new_animals(animal_index, tfidf_matrix):
    async with get_db_session() as db:
        result = await db.execute(select(Animal))
        animals = result.scalars().all()

        # 현재 animal_index에서 기존의 모든 동물 ID를 가져옴
        existing_ids = set(animal_index.keys())
        # 새로운 동물만을 리스트로 생성
        new_animals = [animal for animal in animals if animal.id not in existing_ids]

        # 새로운 동물이 없는 경우, 함수 종료
        if not new_animals:
            return

        # 새로운 동물 데이터로 텍스트 리스트를 생성
        new_texts = [
            f"{animal.shelter_id} {animal.age} {animal.color} {animal.gender} {animal.kind} {animal.region} {animal.special_mark} {animal.happen_place}"
            for animal in new_animals
        ]

    # 새로운 텍스트 데이터를 TF-IDF 벡터로 변환
    new_vectors = vectorizer.transform(new_texts).toarray()

    # 새로운 동물들의 ID 리스트를 생성
    new_ids = [animal.id for animal in new_animals]

    # Milvus에 새로운 데이터 삽입
    animal_collection.insert([new_ids, new_vectors])
    
    # 기존 tfidf_matrix에 새로운 벡터 추가
    current_length = len(tfidf_matrix)
    tfidf_matrix = np.vstack([tfidf_matrix, new_vectors])

    # animal_index를 업데이트하여 새로운 동물들의 ID와 인덱스를 추가
    animal_index.update({animal.id: current_length + idx for idx, animal in enumerate(new_animals)})
    print(f"Updated with new animals")