package com.anmi.grasscutter.modules.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class SendCommandVo implements Serializable {
    private static final long serialVersionUID = -4405188777275585213L;

    private String command;

}
