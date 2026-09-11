package org.rwp.aprilFool;

import org.apache.commons.lang3.ObjectUtils;

import java.sql.Time;

public class ReplacedBlock {
    public Boolean isReplaced = false;
    public Long time = null;
    public ReplacedBlock(long time,Boolean isReplaced) {
        this.time = time;
        this.isReplaced = isReplaced;
    }
}
