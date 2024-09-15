package com.example.demoe.Dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
public class CommentDto {
    private String content;
    private List<String> contextImage=new ArrayList<>();
    private Long commentId;
    private String userEmail;
    private Long productId;
    private LocalDate commentTime;
}
