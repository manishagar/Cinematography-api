package com.cinematography.adminapi.cinematography.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data 
@Builder 
@NoArgsConstructor 
@AllArgsConstructor 
public class RenameDetails {


    private Long itemId;

    private String itemType;

    private String newName;
    
}
