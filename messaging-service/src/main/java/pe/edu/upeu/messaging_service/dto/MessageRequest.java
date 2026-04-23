package pe.edu.upeu.messaging_service.dto;

import jakarta.validation.constraints.NotBlank;

public class MessageRequest {
    @NotBlank
    private String senderUsername;
    @NotBlank
    private String receiverUsername;
    @NotBlank
    private String content;

    public String getSenderUsername() { return senderUsername; }
    public void setSenderUsername(String senderUsername) { this.senderUsername = senderUsername; }
    public String getReceiverUsername() { return receiverUsername; }
    public void setReceiverUsername(String receiverUsername) { this.receiverUsername = receiverUsername; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
}
