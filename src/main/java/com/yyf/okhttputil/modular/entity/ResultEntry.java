package com.yyf.okhttputil.modular.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author wangchen
 * @create 2020/05/14/14:55
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResultEntry {

    private String value;

    private Integer code;
}
