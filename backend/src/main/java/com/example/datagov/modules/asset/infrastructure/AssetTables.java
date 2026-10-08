package com.example.datagov.modules.asset.infrastructure;
/** SQL names and D17 IDs; never accept table names from client input. */
public final class AssetTables {
 private AssetTables() {}
 public static final String DATA_SOURCE = "data_source"; // D17-T09
 public static final String DATA_ASSET = "data_asset"; // D17-T10
 public static final String METADATA_FIELD = "metadata_field"; // D17-T11
}
