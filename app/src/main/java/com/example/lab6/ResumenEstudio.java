package com.example.lab6;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

// Suma horas de los registros guardados. Un curso nulo representa todos los cursos.
public class ResumenEstudio {

    private final Map<String, Double> horasPorMes = new TreeMap<>();
    private final Map<String, Double> horasPorActividad = new LinkedHashMap<>();
    private double totalHoras;

    public ResumenEstudio(List<SesionEstudio> sesiones, String curso) {
        horasPorActividad.put("Teoría", 0.0);
        horasPorActividad.put("Práctica", 0.0);
        horasPorActividad.put("Laboratorio", 0.0);

        for (SesionEstudio sesion : sesiones) {
            if (!sesion.esValida()) {
                continue;
            }
            if (curso != null && !curso.trim().equalsIgnoreCase(sesion.getCurso().trim())) {
                continue;
            }
            String mes = sesion.getFecha().substring(0, 7);
            double horas = sesion.getHoras();
            horasPorMes.put(mes, horasPorMes.getOrDefault(mes, 0.0) + horas);
            String tipo = sesion.getTipoActividad();
            horasPorActividad.put(tipo, horasPorActividad.get(tipo) + horas);
            totalHoras += horas;
        }
    }

    public Map<String, Double> getHorasPorMes() {
        return Collections.unmodifiableMap(horasPorMes);
    }

    public Map<String, Double> getHorasPorActividad() {
        return Collections.unmodifiableMap(horasPorActividad);
    }

    public double getTotalHoras() {
        return totalHoras;
    }
}
