package com.yyf.okhttputil.modular;

import okhttp3.Call;

/**
 * @author wangchen
 * @create 2020/05/14/15:07
 */
public interface OkCallBack {

    void onSuccessful(Call call, String data);

    void onFailure(Call call, String errorMsg);
}
