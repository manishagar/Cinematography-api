package com.cinematography.adminapi.cinematography.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data 
@Builder 
@NoArgsConstructor 
@AllArgsConstructor 
public class MoveDetails {
   private Long itemId;

    private String itemType;

    private Long destinationFolderId;
}
