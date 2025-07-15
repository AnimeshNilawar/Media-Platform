package com.moddynerd.engagementservice.model.like;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LikeDTO {
    private String videoId;
    private String userId;
    private Boolean Like;
}
