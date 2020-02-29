package com.example.bodega.Adaptadores;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

public class BaseAdapter extends SQLiteOpenHelper {
    private static final String DB_NAME ="DB" ;
    private static final int VERSION = 10;

    public static abstract class HABLADORES implements BaseColumns{
        public static final String TABLE_NAME = "HABLADORES";
        public static final String CODIGO = "CODIGO" ;
        public static final String DESCRIPCION = "DESCRIPCION" ;
        public static final String PRECIO = "PRECIO" ;
    }

    public static abstract class PROFORMA implements BaseColumns {
        public static final String  TABLE_NAME="PROFORMA";
        public static final String  ID = "_ID";
        public static final String  FECHA = "FECHA";
        public static final String  COD_CLIENTE = "COD_CLIENTE" ;
        public static final String  NOMBRE_CLIENTE = "NOMBRE_CLIENTE" ;
        public static final String  TOTAL_EXENTO = "TOTAL_EXENTO" ;
        public static final String  TOTAL_GRAVADO = "TOTAL_GRAVADO";
        public static final String  MONTO_IV = "MONTO_IV" ;
        public static final String  TOTAL = "TOTAL";
    }

    public static class DETALLE_PROFORMA implements BaseColumns{
        public static final String TABLE_NAME = "DETALLE_PROFORMA";
        public static final String ID = "_ID" ;
        public static final String REF_PROFORMA = "REF_PROFORMA";
        public static final String COD_ARTICULO = "COD_ARTICULO";
        public static final String ARTICULO = "ARTICULO";
        public static final String PRECIO = "PRECIO";
        public static final String IV = "IV";
        public static final String CANTIDAD = "CANTIDAD";
        public static final String TOTAL = "TOTAL";
    }

    private static final String SENTENCIA_HABLADORES =
            "CREATE TABLE "+ HABLADORES.TABLE_NAME
                    + "(" +
                        HABLADORES.CODIGO + " TEXT (15) NOT NULL,"+
                        HABLADORES.DESCRIPCION + " TEXT (100) NOT NULL,"+
                        HABLADORES.PRECIO + " REAL DEFAULT 0)";

    private static final String SENTENCIA_TABLA_PROFORMA  =
            "CREATE TABLE " + PROFORMA.TABLE_NAME
                    + "(" +
                        PROFORMA.ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"+
                        PROFORMA.FECHA + " TIMESTAMP DEFAULT CURRENT_TIMESTAMP,"+
                        PROFORMA.COD_CLIENTE + " TEXT (30) NOT NULL,"+
                        PROFORMA.NOMBRE_CLIENTE + " TEXT (100) NOT NULL,"+
                        PROFORMA.TOTAL_EXENTO + " REAL NOT NULL DEFAULT 0,"+
                        PROFORMA.TOTAL_GRAVADO + " REAL NOT NULL DEFAULT 0,"+
                        PROFORMA.MONTO_IV + " REAL NOT NULL DEFAULT 0,"+
                        PROFORMA.TOTAL + " REAL NOT NULL DEFAULT 0)" ;

    private static final String SENTENCIA_DETALLE_PROFORMA =
            "CREATE TABLE "+ DETALLE_PROFORMA.TABLE_NAME
                    + "(" +
                        DETALLE_PROFORMA.ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"+
                        DETALLE_PROFORMA.REF_PROFORMA + " TEXT (30) NOT NULL,"+
                        DETALLE_PROFORMA.COD_ARTICULO + " TEXT (30) NOT NULL,"+
                        DETALLE_PROFORMA.ARTICULO + " TEXT (100) NOT NULL,"+
                        DETALLE_PROFORMA.PRECIO + " REAL DEFAULT 0," +
                        DETALLE_PROFORMA.IV + " INT DEFAULT 0," +
                        DETALLE_PROFORMA.CANTIDAD + " REAL DEFAULT 0,"+
                        DETALLE_PROFORMA.TOTAL + " REAL DEFAULT 0)";

    public BaseAdapter(Context context) {
        super(context, DB_NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SENTENCIA_HABLADORES);
        db.execSQL(SENTENCIA_TABLA_PROFORMA);
        db.execSQL(SENTENCIA_DETALLE_PROFORMA);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + HABLADORES.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + PROFORMA.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + DETALLE_PROFORMA.TABLE_NAME);
        onCreate(db);
    }


}
