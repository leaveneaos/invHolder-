package com.rjxx.taxeasy.utils.alipay;

/**
 * Created by Administrator on 2017-06-23.
 */
public class AlipayConstants {

    public static final String GATEWAY_URL = "https://openapi.alipay.com/gateway.do";

//    public static final String URL = "https://openapi.alipaydev.com/gateway.do";

    public static final String AUTH_SCOPE = "auth_user,auth_base,auth_invoice_info";

    /**
     * 沙箱
     */
    //public static final String APP_ID = "2016080600177722";
    /**
     * 泰易发票夹
     */
    public static final String APP_ID = "2017072707922977";
    /**
     * 沙箱
     */
   // public static final String AUTH_URL = "https://openauth.alipaydev.com/oauth2/publicAppAuthorize.htm?app_id=" + APP_ID + "&scope=" + AUTH_SCOPE + "&redirect_uri=ENCODED_URL";
    /**
     * 泰易发票夹
     */
    public static final String AUTH_URL = "https://openauth.alipay.com/oauth2/publicAppAuthorize.htm?app_id=" + APP_ID + "&scope=" + AUTH_SCOPE + "&redirect_uri=ENCODED_URL";


    /**
     * 沙箱
     */
   // public static final String ALIPAY_PUBLIC_KEY = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA3Z1dnd/Z6s5gT8Mls39kSs5nGO5F4mrqDKvo9AV/558XEEC0Re1c3Zbmz66b2T9p3NUAb+5GH0VMUI5D5lc4k9pUu4V2L/Bafttuq6daS8+KaqVMye+noA/0XzVJ1C0Roqamk/4D/sRdFhUqeOvevMarSv5ZiN038Ooc9+Eq9Whlz1/2HMl9dDS8JjISrcY3QXSZacrb+daqFnwa5K2qEDwyZ7+moTq4dQ+10fQrp/CYrsUhHeRO5/N3rFjdlxFCIzx/ntReC48jg4MJhSSV+BmX4ezNz+/oS3fnvCataGa3SeM7/c8X5Qf4g0PIUOCpWoxgd5HBW/tFriYbMJhVYwIDAQAB";
    /**
     * 正式
     */
    public static final String ALIPAY_PUBLIC_KEY = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAk29UBGljh2NkLPJVwfh0QUJAObfThUSUPC04ohVVCvMMmsN1WsJopxXOIa/QaNw12ew3YzCZ4VYUMfVdHNsgVT6LVzyGOKjPzwocP51upzpM+0j0qa1uuXvVvdyzA4fug9cUPCV2zLQTnBRmtaxOzKwh29XELtL+rRaSBIuwo+lVCjOFoD9psmfIAjtXtquQXesMmFn1ItRJ6y5/Aj4XnrCpeneuBcA0c8XOUvxgGFYdN2Xs28tSU/tXDiVSijjZD01cQFXqKxPJTEAO0AlP1yThA4NOqqYwHA/0RQC+Xuyv9XPz+iIFGvVJmE51haWfoDoLnKpVsEceSXrFrUCFjQIDAQAB";

    //public static final String PUBLIC_KEY = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAwexZ5yU0vMCVloBx7G7ERmpqTljWlwlolrGsR2EIq7lnGCa4/ty3eSyUws/MlV9w5m+CijUyORYPR0UAIPY2O63kflPNMPYboV3BU7hfdI8HcCW6wnhHMXNRzRWa93y/hB9VjOnRDVmc22j/1zgiUWavYsbMx3K1cwxSurO3vVbJ7UZZJYhBuJPfVopHjO1INYgPPY4WOaJFHXfXD5l+EcnUrfP1fsvlpql5Ks0x1BEUQdH+X8RuZ2MNMNu8gkovrqcHLEPVtLwBXQDQDhgOv0xO2qct1aNwigsKjF3bMybFqOI+UWpi0ntH11BlSzA4U8qFoK7cPWVrcbBL5rqq4QIDAQAB";

    public static final String PRIVATE_KEY = "MIIEvwIBADANBgkqhkiG9w0BAQEFAASCBKkwggSlAgEAAoIBAQCTb1QEaWOHY2Qs8lXB+HRBQkA5t9OFRJQ8LTiiFVUK8wyaw3VawminFc4hr9Bo3DXZ7DdjMJnhVhQx9V0c2yBVPotXPIY4qM/PChw/nW6nOkz7SPSprW65e9W93LMDh+6D1xQ8JXbMtBOcFGa1rE7MrCHb1cQu0v6tFpIEi7Cj6VUKM4WgP2myZ8gCO1e2q5Bd6wyYWfUi1EnrLn8CPheesKl6d64FwDRzxc5S/GAYVh03Zezby1JT+1cOJVKKONkPTVxAVeorE8lMQA7QCU/XJOEDg06qpjAcD/RFAL5e7K/1c/P6IgUa9UmYTnWFpZ+gOgucqlWwRx5JesWtQIWNAgMBAAECggEBAIJ1hWut8cMQYMkg2l+cQQqp2/j2v7VyHJUvEqjhpXHd5W5gvExGMKKAufi3APx4tzIRQmWD0PZC2/Xk/rKU94RWpqfXx5EFV3UFpj9oSEyzWwET6mLmM07v1A9/5eue8aA5xg7+u6OhSKTYrJXfVY7oVcGX41PnojF5AlYBYZl3e7VdNsWsHjrWo6vqpftYhIW+BFRVpsamoBbHXpY/ogKxS1FOUNUoGo7/1qFG4CJyjuCCpBfejJdxIOCQEHFrHHBn1Uw6uQBY4mpBRgOHoAJp67Y0TIsDMIJ5hi6BAdPQTT85s0/vjG97BGsC0L1/lwKerZBWGl04ZlL0E6IMZWECgYEAx5mEh6uXBTLLuS6u0LDRZWfdMpJlMJmV7QzADNzbCMufxwbKLLSr+XLCPl9ahMcnilF4kn3ybFir6MiAXuVuzYqvh4pryxdxsjhpQ3FgisZd1V4aHLdj6HFd0OTnz9R6LJ8MuV6wWYWHtNDxD6cwcanq7rlvTLQ4Z5yRs/5gfAUCgYEAvRhax+Rfjdp+1hNnJ6grnZK2dTnW7gWkTrW5N3mTEZGf185QIkedWrDNFJjwYS9LGCYcg73B/ngFQLvQ1gZcKRIYTZZqygbspJU99IW7v+nmRwHPcKFJovbOQ6vsjlY9upY4Gz5zAZYgwabdSnZnEZE6SffcZhooC72pXrcXIekCgYEAo8DAKSpOaRZmaxPwmggmksJmiCHw4LFwfh1yxlq/XHTqLbfVUF4dpTZQRCmosrxNJT534/9uTxNkXkN4heJHSFwaa+HVU7Z1Mdj0Cz/OC6x4uR9N3vdaAQ5ZAJN711cjsM3/+2jC138keeTdkJECvo2/z4DU3iIREHg0VW5BrB0CgYEAh7j2V6kMCKX+CauNi8JB4+yM92DXaqoz/6ahVTb2n1KVlvDnRWN/hruFxzXquuaWELQyHUDS/9Gne4oS37ta5MOvG+lNpsoHii3wdRST6LB6bxbWjHHHh6d77sh0lslBry3pAWz2YPofcr9Q8ARCADLr0nGMCODXQ24TJf9mJBkCgYAmESs8k5zCN3XEYZ0LoF1e0WyXAxqIEk0VurcSWbT7UTRydazkQVTeMxpvcJl8fB0D1Cbu3cZSrz+/F+yNsglLhHvE5Nk2jsfBGwimEpwH0Ppg+GjPyXwsGoaHLHykdzrEPvPbwtfIhB3FheL8eKix8G5Z02vnpNbKfhCg0xcD5w==";

    public static final String FORMAT = "json";

    public static final String CHARSET = "UTF-8";

    public static final String SIGN_TYPE = "RSA2";

    public static final String ALIPAY_ACCESS_TOKEN = "alipay_access_token";

    public static final String ALIPAY_USER_ID = "alipay_user_id";

    public static final String AFTER_ALIPAY_AUTHORIZED_REDIRECT_URL = "/getAlipay";
}
