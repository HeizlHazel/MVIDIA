package com.kh.mvidia.notion.dto;

import com.kh.mvidia.approval.model.dto.ApprovalItem;
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
