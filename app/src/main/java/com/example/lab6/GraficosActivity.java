package com.example.lab6;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.lab6.databinding.ActivityGraficosBinding;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.google.android.material.color.MaterialColors;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeSet;

public class GraficosActivity extends AppCompatActivity {

    private ActivityGraficosBinding binding;
    private ListenerRegistration listener;
    private List<SesionEstudio> sesiones = new ArrayList<>();
    private final List<String> cursos = new ArrayList<>();
    private String cursoSeleccionado;
    private boolean datosCargados;

    @Override
    protected void onCreate(Bundle estado) {
        super.onCreate(estado);
        EdgeToEdge.enable(this);
        binding = ActivityGraficosBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (vista, insets) -> {
            Insets barras = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            vista.setPadding(barras.left, barras.top, barras.right, barras.bottom);
            return insets;
        });
        if (estado != null) {
            cursoSeleccionado = estado.getString("curso");
        }
        binding.btnVolver.setOnClickListener(view -> finish());
        binding.filtroCurso.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int posicion, long id) {
                if (!datosCargados || posicion >= cursos.size()) {
                    return;
                }
                cursoSeleccionado = posicion == 0 ? null : cursos.get(posicion);
                mostrarGraficos();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        configurarGraficos();
    }

    @Override
    protected void onStart() {
        super.onStart();
        datosCargados = false;
        sesiones = new ArrayList<>();
        binding.filtroCurso.setEnabled(false);
        FirebaseUser usuario = FirebaseAuth.getInstance().getCurrentUser();
        if (usuario == null) {
            startActivity(new Intent(this, CustomLoginActivity.class)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
            finish();
            return;
        }
        binding.txtEstadoGraficos.setText(R.string.cargando_sesiones);
        binding.txtEstadoGraficos.setVisibility(View.VISIBLE);
        binding.contenidoGraficos.setVisibility(View.GONE);
        String uid = usuario.getUid();
        listener = FirebaseFirestore.getInstance().collection("usuarios").document(uid)
                .collection("sesiones").addSnapshotListener((resultado, error) -> {
                    FirebaseUser actual = FirebaseAuth.getInstance().getCurrentUser();
                    if (actual == null || !uid.equals(actual.getUid())) {
                        mostrarError(R.string.sesion_finalizada);
                        return;
                    }
                    if (error != null || resultado == null) {
                        Log.e("Firestore", "No se pudieron cargar los gráficos", error);
                        mostrarError(R.string.error_cargar_sesiones);
                        return;
                    }
                    try {
                        sesiones = resultado.toObjects(SesionEstudio.class);
                        for (SesionEstudio sesion : sesiones) {
                            if (!sesion.esValida()) {
                                throw new IllegalArgumentException("Registro inválido");
                            }
                        }
                    } catch (RuntimeException conversionError) {
                        Log.e("Firestore", "Datos incompatibles para los gráficos", conversionError);
                        mostrarError(R.string.error_formato_sesiones);
                        return;
                    }
                    datosCargados = true;
                    binding.filtroCurso.setEnabled(true);
                    actualizarCursos();
                    mostrarGraficos();
                });
    }

    @Override
    protected void onStop() {
        if (listener != null) {
            listener.remove();
            listener = null;
        }
        super.onStop();
    }

    @Override
    protected void onSaveInstanceState(Bundle estado) {
        estado.putString("curso", cursoSeleccionado);
        super.onSaveInstanceState(estado);
    }

    private void configurarGraficos() {
        int texto = MaterialColors.getColor(binding.graficoBarras,
                com.google.android.material.R.attr.colorOnSurface);
        binding.graficoBarras.getDescription().setEnabled(false);
        binding.graficoBarras.getLegend().setEnabled(false);
        binding.graficoBarras.getAxisRight().setEnabled(false);
        binding.graficoBarras.getAxisLeft().setAxisMinimum(0);
        binding.graficoBarras.getAxisLeft().setTextColor(texto);
        binding.graficoBarras.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        binding.graficoBarras.getXAxis().setGranularity(1);
        binding.graficoBarras.getXAxis().setDrawGridLines(false);
        binding.graficoBarras.getXAxis().setTextColor(texto);
        binding.graficoBarras.setScaleEnabled(false);
        binding.graficoBarras.setPinchZoom(false);
        binding.graficoBarras.setFitBars(true);
        binding.graficoTorta.getDescription().setEnabled(false);
        binding.graficoTorta.setUsePercentValues(true);
        binding.graficoTorta.setDrawHoleEnabled(false);
        binding.graficoTorta.setDrawEntryLabels(false);
        binding.graficoTorta.getLegend().setTextColor(texto);
        binding.graficoTorta.getLegend().setWordWrapEnabled(true);
        binding.graficoTorta.setRotationEnabled(false);
    }

    private void actualizarCursos() {
        TreeSet<String> nombres = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        for (SesionEstudio sesion : sesiones) {
            nombres.add(sesion.getCurso().trim());
        }
        cursos.clear();
        cursos.add(getString(R.string.todos_cursos));
        cursos.addAll(nombres);
        int posicion = 0;
        for (int i = 1; i < cursos.size(); i++) {
            if (cursos.get(i).equalsIgnoreCase(cursoSeleccionado)) {
                posicion = i;
                break;
            }
        }
        cursoSeleccionado = posicion == 0 ? null : cursos.get(posicion);
        binding.filtroCurso.setAdapter(new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, cursos));
        binding.filtroCurso.setSelection(posicion);
    }

    private void mostrarGraficos() {
        ResumenEstudio resumen = new ResumenEstudio(sesiones, cursoSeleccionado);
        if (resumen.getTotalHoras() == 0) {
            binding.txtEstadoGraficos.setText(R.string.sin_datos_graficos);
            binding.txtEstadoGraficos.setVisibility(View.VISIBLE);
            binding.contenidoGraficos.setVisibility(View.GONE);
            return;
        }
        binding.txtEstadoGraficos.setVisibility(View.GONE);
        binding.contenidoGraficos.setVisibility(View.VISIBLE);

        int texto = MaterialColors.getColor(binding.graficoBarras,
                com.google.android.material.R.attr.colorOnSurface);
        int primario = MaterialColors.getColor(binding.graficoBarras,
                androidx.appcompat.R.attr.colorPrimary);
        List<BarEntry> barras = new ArrayList<>();
        List<String> meses = new ArrayList<>();
        String[] nombresMeses = getResources().getStringArray(R.array.meses);
        for (Map.Entry<String, Double> mes : resumen.getHorasPorMes().entrySet()) {
            barras.add(new BarEntry(barras.size(), mes.getValue().floatValue()));
            int numeroMes = Integer.parseInt(mes.getKey().substring(5, 7));
            meses.add(nombresMeses[numeroMes - 1] + " " + mes.getKey().substring(0, 4));
        }
        BarDataSet datosBarras = new BarDataSet(barras, getString(R.string.horas_dedicadas));
        datosBarras.setColor(primario);
        datosBarras.setValueTextColor(texto);
        BarData datos = new BarData(datosBarras);
        datos.setBarWidth(0.6f);
        binding.graficoBarras.setData(datos);
        binding.graficoBarras.getXAxis().setValueFormatter(new IndexAxisValueFormatter(meses));
        binding.graficoBarras.getXAxis().setLabelRotationAngle(-35);
        binding.graficoBarras.getXAxis().setLabelCount(Math.min(meses.size(), 6));
        binding.graficoBarras.setVisibleXRangeMaximum(6);
        binding.graficoBarras.setExtraBottomOffset(20);
        binding.graficoBarras.invalidate();

        List<PieEntry> porciones = new ArrayList<>();
        for (Map.Entry<String, Double> tipo : resumen.getHorasPorActividad().entrySet()) {
            if (tipo.getValue() > 0) {
                porciones.add(new PieEntry(tipo.getValue().floatValue(), tipo.getKey()));
            }
        }
        PieDataSet datosTorta = new PieDataSet(porciones, "");
        datosTorta.setColors(new int[]{0xFF3155A6, 0xFF12766A, 0xFF9B4F00});
        datosTorta.setValueTextColor(android.graphics.Color.WHITE);
        datosTorta.setValueTextSize(14);
        datosTorta.setSliceSpace(2);
        PieData torta = new PieData(datosTorta);
        torta.setValueFormatter(new PercentFormatter(binding.graficoTorta));
        binding.graficoTorta.setData(torta);
        binding.graficoTorta.invalidate();

        NumberFormat numero = NumberFormat.getNumberInstance(Locale.forLanguageTag("es-PE"));
        numero.setMaximumFractionDigits(2);
        binding.txtTotalHoras.setText(getString(R.string.total_horas, numero.format(resumen.getTotalHoras())));
        binding.graficoBarras.setContentDescription(getString(R.string.resumen_barras, resumen.getHorasPorMes().toString()));
        binding.graficoTorta.setContentDescription(getString(R.string.resumen_torta, resumen.getHorasPorActividad().toString()));
    }

    private void mostrarError(int mensaje) {
        datosCargados = false;
        binding.filtroCurso.setEnabled(false);
        sesiones = new ArrayList<>();
        binding.txtEstadoGraficos.setText(mensaje);
        binding.txtEstadoGraficos.setVisibility(View.VISIBLE);
        binding.contenidoGraficos.setVisibility(View.GONE);
    }
}
