package com.hong.ForPaw.controller.DTO;

import java.time.LocalDateTime;
import java.util.List;

public class ChatResponse {

    public record FindMessageListInRoomDTO(Long lastMessageId, List<ChatResponse.MessageDTD> messages) {}

    public record MessageDTD(String messageId,
                             String senderName,
                             String content,
                             LocalDateTime date,
                             boolean isMine) {}

    public record FindChatRoomDrawerDTO(List<ChatImageDTO> images, List<ChatUserDTO> users) {}

    public record ChatImageDTO(String imageURL) {}

    public record ChatUserDTO(String userName) {}

    public record FindChatRoomListDTO(List<ChatResponse.RoomDTO> rooms) {}

    public record RoomDTO(Long chatRoomId,
                          String name,
                          String lastMessageContent,
                          LocalDateTime lastMessageTime,
                          Long offset) {}

    public record FindChatRoomImagesDTO(List<ChatImageDTO> images) {}
}
