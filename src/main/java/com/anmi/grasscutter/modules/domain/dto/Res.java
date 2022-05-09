package com.anmi.grasscutter.modules.domain.dto;

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
public class Res implements Serializable {

    private int code;
    private String msg;
    private String err;
    private Object data;

    public static Res ok() {
        return Res.builder().code(200).build();
    }

    public static Res err(String err) {
        return Res.builder().code(500).err(err).build();
    }
}
