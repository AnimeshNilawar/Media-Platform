package com.moddynerd.engagementservice.model.comment;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GetCommentDTO {
    private Integer id;
    private String videoId;
    private String username;
    private String content;
    private String updatedAt;
}
