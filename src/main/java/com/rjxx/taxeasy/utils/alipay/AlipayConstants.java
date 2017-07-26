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
    public static final String APP_ID = "2017062207544676";
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
    public static final String ALIPAY_PUBLIC_KEY = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAg5gJd4KO74VYExgBEkifXVsbqjMRv6gP3tQor/O4I3m5kloJazaI4eq4ST8beKtG65dz5qnalT7Epc86F+JOoR2lRrSqYmPz9pyfgHG9+0dOEHaa5Lq56GmUqM7WKAkLBWKMsI8EVV/DVVkenPaLo/rLfpqSYt1YFVnEJEb5xTNVGX6uvwmHhXzqiFwCwa9JYoMUKiWI8REon/wo0GKwftXOXBa/SPmoxcsfJfrnH4WCCNhxHMY/CxjeM4O6skzeLr+wL3TcmjiPiXu0pRc8gYx5ySWgcd+PdvkkJt5LVMSriME/mYiGIBV6/TMgaKJ6wS3MX67PbILjqRj57gdblwIDAQAB";

    public static final String PUBLIC_KEY = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAwexZ5yU0vMCVloBx7G7ERmpqTljWlwlolrGsR2EIq7lnGCa4/ty3eSyUws/MlV9w5m+CijUyORYPR0UAIPY2O63kflPNMPYboV3BU7hfdI8HcCW6wnhHMXNRzRWa93y/hB9VjOnRDVmc22j/1zgiUWavYsbMx3K1cwxSurO3vVbJ7UZZJYhBuJPfVopHjO1INYgPPY4WOaJFHXfXD5l+EcnUrfP1fsvlpql5Ks0x1BEUQdH+X8RuZ2MNMNu8gkovrqcHLEPVtLwBXQDQDhgOv0xO2qct1aNwigsKjF3bMybFqOI+UWpi0ntH11BlSzA4U8qFoK7cPWVrcbBL5rqq4QIDAQAB";

    public static final String PRIVATE_KEY = "MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQDB7FnnJTS8wJWWgHHsbsRGampOWNaXCWiWsaxHYQiruWcYJrj+3Ld5LJTCz8yVX3Dmb4KKNTI5Fg9HRQAg9jY7reR+U80w9huhXcFTuF90jwdwJbrCeEcxc1HNFZr3fL+EH1WM6dENWZzbaP/XOCJRZq9ixszHcrVzDFK6s7e9VsntRlkliEG4k99WikeM7Ug1iA89jhY5okUdd9cPmX4RydSt8/V+y+WmqXkqzTHUERRB0f5fxG5nYw0w27yCSi+upwcsQ9W0vAFdANAOGA6/TE7apy3Vo3CKCwqMXdszJsWo4j5RamLSe0fXUGVLMDhTyoWgrtw9ZWtxsEvmuqrhAgMBAAECggEBALbxQ7b9sIzImdYLgm3r6RoM1hDnWjnOVG6lWsNT1Rw7ofh0v10f165m0kgeRQA9s4KQe/PqT2DvYKZ5f6+Y1nzihYZvhEnGQzat/e3/J34MWJYB4fgIBBGwmuEAmtsTQpFgBhMTmXYgrguHOKROfoGQf7CNny4hFvBcFcewN4xTqBxtw18aWZRfrHX10Y0Rg+5eVAgTLKWBEqRC1l4nkxmnihs6QmNvbz9tYcwIIZA4uDSemJD5nicKQZj9vwtEgeXPdM51XjWZUhSc4vQxGCPVXj9R0E6f/EG9P1FRBSoq9zv7aVijnraG2ypXwSTfrxS0/Qop0mmsYzYQVsVyMYkCgYEA8Uv7sjAfnYm88mZ0Wmkqo4mCnxxVoygdH7lArLcnpwXY5R3b7r5YDNUHcqqr6QW0jIFrDEPyjs3XP/AUIHXIEh/BXMCW0VFDAynqcyDK8uaEWdMAFji3f52J1I3WT+F2Y+fEOmzrtrC2k5jblDSUZn7dyLDiaE4P1DYGjvP11X8CgYEAzb1h37C1TG5m04pj2v3ecp7HQqOOE0A8caHFyJBimOPueA1a+VqX3Alnp1eq+JeHnbue2RFZ+vVgNNJ5tyhcMuXWQopOw8x06V9SdDWjYc2tihlMxyIoikDcM5cgsDt0AV1EUaudY/ORmfZhW5lNAbRQDGp5bi51nGP0FY0Hb58CgYBm/u3yw0PbdN2oAj+MD/PZVmrhC7EqMYcs8WA/2PYd/wrz0Wj9YVYoNA43zc4PaxYEG4Hb/Gzes9I7Qnj02hGKWu+obbRyqGMYSJ4AgnDeebLovH3+/jqlSFN63QgrlKt6PYWvAqsikg98tPSVMFRf46s7bPVXXGq5RE5MWqoGdQKBgFwABdY7dLb632xVBBYXU/O5YK1B6fRd0ymVB5aKSizIMAjrH/VFN4cjhYBlSYTkbbZxAbVbdvsr4pt8SwVGPQRdlpQmuNYBhX4eLAbfPXIswR9x9M1PTwWTv2QqfmmQDG3XwQcsULMzZbaNYDfPs4nBkdIbgrVsfRcz8x/38o1NAoGAOkxOrf66MZNx0d79X3OiskqM2zlnb0FO44febEnDhyd2MC/wiB1yOXkOtMMMA3ZWROiuaahnv1lOyyiAnVZKNSJTs9MB9alz+d7W5D4EqSy/PYUB1Qpkv/hpgpq74/TCGFgLnJG2hguHCrc92m03fsN+IEzr1Hhzx3coI6QJgN0=";

    public static final String FORMAT = "json";

    public static final String CHARSET = "UTF-8";

    public static final String SIGN_TYPE = "RSA2";

    public static final String ALIPAY_ACCESS_TOKEN = "alipay_access_token";

    public static final String ALIPAY_USER_ID = "alipay_user_id";

    public static final String AFTER_ALIPAY_AUTHORIZED_REDIRECT_URL = "/getAlipay";
}
