package com.kh.mvidia.approval.model.dto;

import lombok.*;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class NotionPageResult {

    private List<ApprovalItem> results;
    private boolean hasMore;
    private String nextCursor;

}
