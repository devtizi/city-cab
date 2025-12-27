package com.citycab.app.common;

import java.time.ZoneId;

public class AssetsData {
    public static ZoneId johannesburgZone = ZoneId.of("Africa/Johannesburg");
    public static ZoneId systemDefault = ZoneId.systemDefault();
    public static final String privateKeyPath = "/etc/citycab/keys/private_key.pem";
    public static final String publicKeyPath = "/etc/citycab/keys/public_key.pem";
}