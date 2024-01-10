package com.github.sibmaks.itmodb.hw3.api.rq;

import com.github.sibmaks.itmodb.hw3.dto.DataImportStatus;
import lombok.*;

import java.io.Serializable;

/**
 * @author sibmaks
 * @since 0.0.1
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImportStateRs implements Serializable {
    private String taskId;
    private int successProcessedRowsCount;
    private int failedProcessedRowsCount;
    private long startTime;
    private Long finishTime;
    private DataImportStatus status;

}
