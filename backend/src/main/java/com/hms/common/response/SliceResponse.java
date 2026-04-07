package com.hms.common.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class SliceResponse<T> {
    private List<T> content;
    private int page;
    private int size;
    private boolean first;
    private boolean last;
    private boolean hasNext;
    private int numberOfElements;
}
