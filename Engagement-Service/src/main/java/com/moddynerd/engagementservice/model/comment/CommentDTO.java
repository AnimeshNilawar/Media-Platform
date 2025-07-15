package com.moddynerd.engagementservice.model.comment;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CommentDTO {
    private Integer id;
    private String videoId;
    private String userId;
    private String content;
}
