package com.example.lab6;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.example.lab6.databinding.ActivityFormularioSesionBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.TreeSet;

public class FormularioSesionActivity extends AppCompatActivity {

    public static final String EXTRA_SESION_ID = "sesion_id";
    private ActivityFormularioSesionBinding binding;
    private FormularioSesionViewModel modelo;
    private DocumentReference documento;
    private String uid;
    private String fecha;
    private boolean editando;
    private boolean cargado;
    private final List<String> cursosConocidos = new ArrayList<>();

    @Override
    protected void onCreate(Bundle estado) {
        super.onCreate(estado);
        EdgeToEdge.enable(this);
        binding = ActivityFormularioSesionBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (vista, insets) -> {
            Insets barras = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            vista.setPadding(barras.left, barras.top, barras.right, barras.bottom);
            return insets;
        });

        FirebaseUser usuario = FirebaseAuth.getInstance().getCurrentUser();
        if (usuario == null) {
            volverAlInicio();
            return;
        }
        uid = usuario.getUid();
        if (estado != null && !uid.equals(estado.getString("uid"))) {
            volverAlInicio();
            return;
        }

        modelo = new ViewModelProvider(this).get(FormularioSesionViewModel.class);
        String id = getIntent().getStringExtra(EXTRA_SESION_ID);
        editando = id != null;
        if (estado != null) {
            id = estado.getString("documento_id");
        }
        CollectionReference sesiones = FirebaseFirestore.getInstance().collection("usuarios")
                .document(uid).collection("sesiones");
        documento = id == null ? sesiones.document() : sesiones.document(id);

        binding.txtTituloFormulario.setText(editando ? R.string.editar_registro : R.string.nuevo_registro);
        binding.btnVolver.setOnClickListener(view -> finish());
        binding.btnCancelar.setOnClickListener(view -> finish());
        binding.btnGuardar.setOnClickListener(view -> guardar());
        binding.campoFecha.setOnClickListener(view -> elegirFecha());
        binding.campoFecha.setShowSoftInputOnFocus(false);

        binding.campoTipo.setAdapter(new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, getResources().getStringArray(R.array.tipos_actividad)));

        if (estado != null && estado.getBoolean("cargado")) {
            cargado = true;
            fecha = estado.getString("fecha");
            mostrarFecha();
        } else if (editando) {
            cargado = false;
        } else {
            Calendar hoy = Calendar.getInstance();
            fecha = String.format(Locale.ROOT, "%04d-%02d-%02d", hoy.get(Calendar.YEAR),
                    hoy.get(Calendar.MONTH) + 1, hoy.get(Calendar.DAY_OF_MONTH));
            binding.campoTipo.setText(getResources().getStringArray(R.array.tipos_actividad)[0], false);
            cargado = true;
            mostrarFecha();
        }
        actualizarCarga();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (modelo != null && modelo.guardado != null) {
            observarGuardado();
        } else if (modelo != null && !cargado) {
            cargarRegistro();
        }
        if (documento != null) {
            cargarCursos();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle estado) {
        if (documento != null) {
            estado.putString("uid", uid);
            estado.putString("documento_id", documento.getId());
            estado.putString("fecha", fecha);
            estado.putBoolean("cargado", cargado);
        }
        super.onSaveInstanceState(estado);
    }

    private void cargarRegistro() {
        documento.get().addOnCompleteListener(this, resultado -> {
            if (!resultado.isSuccessful() || resultado.getResult() == null) {
                Toast.makeText(this, R.string.error_cargar_registro, Toast.LENGTH_LONG).show();
                finish();
                return;
            }
            if (!resultado.getResult().exists()) {
                Toast.makeText(this, R.string.registro_no_existe, Toast.LENGTH_LONG).show();
                finish();
                return;
            }
            try {
                SesionEstudio sesion = resultado.getResult().toObject(SesionEstudio.class);
                if (sesion == null || !sesion.esValida()) {
                    throw new IllegalArgumentException("Registro inválido");
                }
                binding.campoCurso.setText(sesion.getCurso(), false);
                binding.campoHoras.setText(String.valueOf(sesion.getHoras()));
                binding.campoTipo.setText(sesion.getTipoActividad(), false);
                fecha = sesion.getFecha();
                cargado = true;
                mostrarFecha();
                actualizarCarga();
            } catch (RuntimeException error) {
                Log.e("Firestore", "Error al leer el registro", error);
                Toast.makeText(this, R.string.error_formato_sesiones, Toast.LENGTH_LONG).show();
                finish();
            }
        });
    }

    private void cargarCursos() {
        documento.getParent().get().addOnSuccessListener(this, resultado -> {
            TreeSet<String> cursos = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
            for (com.google.firebase.firestore.DocumentSnapshot registro : resultado) {
                Object curso = registro.get("curso");
                if (curso instanceof String && !((String) curso).trim().isEmpty()) {
                    cursos.add(((String) curso).trim());
                }
            }
            cursosConocidos.clear();
            cursosConocidos.addAll(cursos);
            binding.campoCurso.setAdapter(new ArrayAdapter<>(this,
                    android.R.layout.simple_dropdown_item_1line, cursosConocidos));
        });
    }

    private void elegirFecha() {
        Calendar calendario = Calendar.getInstance();
        if (SesionEstudio.fechaValida(fecha)) {
            String[] partes = fecha.split("-");
            calendario.set(Integer.parseInt(partes[0]), Integer.parseInt(partes[1]) - 1,
                    Integer.parseInt(partes[2]));
        }
        new DatePickerDialog(this, (vista, anio, mes, dia) -> {
            fecha = String.format(Locale.ROOT, "%04d-%02d-%02d", anio, mes + 1, dia);
            binding.inputFecha.setError(null);
            mostrarFecha();
        }, calendario.get(Calendar.YEAR), calendario.get(Calendar.MONTH),
                calendario.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void mostrarFecha() {
        if (SesionEstudio.fechaValida(fecha)) {
            binding.campoFecha.setText(getString(R.string.fecha_visible,
                    fecha.substring(8), fecha.substring(5, 7), fecha.substring(0, 4)));
        }
    }

    private void guardar() {
        if (!cargado || modelo.guardado != null) {
            return;
        }
        binding.inputCurso.setError(null);
        binding.inputFecha.setError(null);
        binding.inputHoras.setError(null);
        binding.inputTipo.setError(null);

        String curso = binding.campoCurso.getText().toString().trim();
        if (curso.isEmpty()) {
            binding.inputCurso.setError(getString(R.string.ingresa_curso));
            return;
        }
        if (!SesionEstudio.fechaValida(fecha)) {
            binding.inputFecha.setError(getString(R.string.selecciona_fecha));
            return;
        }
        double horas;
        try {
            horas = Double.parseDouble(binding.campoHoras.getText().toString().trim().replace(',', '.'));
        } catch (NumberFormatException error) {
            binding.inputHoras.setError(getString(R.string.horas_invalidas));
            return;
        }
        if (!Double.isFinite(horas) || horas <= 0) {
            binding.inputHoras.setError(getString(R.string.horas_invalidas));
            return;
        }
        String tipo = binding.campoTipo.getText().toString();
        for (String existente : cursosConocidos) {
            if (existente.equalsIgnoreCase(curso)) {
                curso = existente;
                break;
            }
        }
        SesionEstudio sesion = new SesionEstudio();
        sesion.setCurso(curso);
        sesion.setFecha(fecha);
        sesion.setHoras(horas);
        sesion.setTipoActividad(tipo);
        if (!sesion.esValida()) {
            binding.inputTipo.setError(getString(R.string.selecciona_tipo));
            return;
        }
        FirebaseUser usuario = FirebaseAuth.getInstance().getCurrentUser();
        if (usuario == null || !uid.equals(usuario.getUid())) {
            volverAlInicio();
            return;
        }

        // El ID se conserva al reintentar para evitar duplicar un registro nuevo.
        modelo.guardado = editando ? documento.update(sesion.aMapa()) : documento.set(sesion.aMapa());
        observarGuardado();
    }

    private void observarGuardado() {
        actualizarCarga();
        modelo.guardado.addOnCompleteListener(this, resultado -> {
            modelo.guardado = null;
            actualizarCarga();
            if (resultado.isSuccessful()) {
                Toast.makeText(this, R.string.registro_guardado, Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Log.e("Firestore", "No se pudo guardar el registro", resultado.getException());
                Exception error = resultado.getException();
                if (error instanceof FirebaseFirestoreException
                        && ((FirebaseFirestoreException) error).getCode()
                        == FirebaseFirestoreException.Code.NOT_FOUND) {
                    Toast.makeText(this, R.string.registro_no_existe, Toast.LENGTH_LONG).show();
                    finish();
                    return;
                }
                Toast.makeText(this, R.string.error_guardar_registro, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void actualizarCarga() {
        boolean ocupado = !cargado || modelo.guardado != null;
        binding.progreso.setVisibility(ocupado ? View.VISIBLE : View.GONE);
        binding.btnGuardar.setEnabled(!ocupado);
        binding.campoCurso.setEnabled(!ocupado);
        binding.campoFecha.setEnabled(!ocupado);
        binding.campoHoras.setEnabled(!ocupado);
        binding.campoTipo.setEnabled(!ocupado);
        binding.txtEstadoGuardado.setVisibility(modelo.guardado != null ? View.VISIBLE : View.GONE);
    }

    private void volverAlInicio() {
        startActivity(new Intent(this, CustomLoginActivity.class)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
        finish();
    }
}
