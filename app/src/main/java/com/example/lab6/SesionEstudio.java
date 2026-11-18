package com.example.lab6;

import com.google.firebase.firestore.DocumentId;

import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class SesionEstudio {

    private String id;
    private String curso;
    private String fecha;
    private Double horas;
    private String tipoActividad;

    public SesionEstudio() {
    }

    @DocumentId
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCurso() {
        return curso;
    }

    public void setCurso(String curso) {
        this.curso = curso;
    }

    public String getFecha() {
        return fecha;
    }

    public void setFecha(String fecha) {
        this.fecha = fecha;
    }

    public Double getHoras() {
        return horas;
    }

    public void setHoras(Double horas) {
        this.horas = horas;
    }

    public String getTipoActividad() {
        return tipoActividad;
    }

    public void setTipoActividad(String tipoActividad) {
        this.tipoActividad = tipoActividad;
    }

    public boolean esValida() {
        return curso != null && !curso.trim().isEmpty()
                && fechaValida(fecha)
                && horas != null && Double.isFinite(horas) && horas > 0
                && Arrays.asList("Teoría", "Práctica", "Laboratorio").contains(tipoActividad);
    }

    public static boolean fechaValida(String fecha) {
        if (fecha == null || !fecha.matches("[0-9]{4}-[0-9]{2}-[0-9]{2}")) {
            return false;
        }
        SimpleDateFormat formato = new SimpleDateFormat("yyyy-MM-dd", Locale.ROOT);
        formato.setLenient(false);
        ParsePosition posicion = new ParsePosition(0);
        return formato.parse(fecha, posicion) != null && posicion.getIndex() == fecha.length();
    }

    // Firestore genera el ID del documento. Los campos guardados son los del laboratorio.
    public Map<String, Object> aMapa() {
        Map<String, Object> datos = new HashMap<>();
        datos.put("curso", curso.trim());
        datos.put("fecha", fecha);
        datos.put("horas", horas);
        datos.put("tipoActividad", tipoActividad);
        return datos;
    }
}
