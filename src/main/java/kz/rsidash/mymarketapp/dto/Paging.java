package kz.rsidash.mymarketapp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Paging {
    private int pageSize;
    private int pageNumber;
    private boolean hasPrevious;
    private boolean hasNext;
}
