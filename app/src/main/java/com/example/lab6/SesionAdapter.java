package com.example.lab6;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lab6.databinding.ItemSesionBinding;

import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SesionAdapter extends RecyclerView.Adapter<SesionAdapter.SesionViewHolder> {

    private final List<SesionEstudio> sesiones = new ArrayList<>();
    private final NumberFormat formatoHoras = NumberFormat.getNumberInstance(Locale.forLanguageTag("es-PE"));
    private final SimpleDateFormat fechaGuardada = new SimpleDateFormat("yyyy-MM-dd", Locale.ROOT);
    private final SimpleDateFormat fechaVisible = new SimpleDateFormat("dd/MM/yyyy", Locale.ROOT);
    private final Acciones acciones;

    public interface Acciones {
        void editar(SesionEstudio sesion);
        void eliminar(SesionEstudio sesion);
    }

    public SesionAdapter(Acciones acciones) {
        this.acciones = acciones;
        fechaGuardada.setLenient(false);
        formatoHoras.setMaximumFractionDigits(2);
    }

    public void actualizarSesiones(List<SesionEstudio> nuevasSesiones) {
        sesiones.clear();
        sesiones.addAll(nuevasSesiones);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public SesionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemSesionBinding binding = ItemSesionBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new SesionViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull SesionViewHolder holder, int position) {
        SesionEstudio sesion = sesiones.get(position);
        ItemSesionBinding binding = holder.binding;

        binding.txtCurso.setText(sesion.getCurso());
        binding.txtFecha.setText(formatearFecha(sesion.getFecha()));
        binding.txtTipoActividad.setText(sesion.getTipoActividad());
        binding.btnEditar.setOnClickListener(view -> acciones.editar(sesion));
        binding.btnEliminar.setOnClickListener(view -> acciones.eliminar(sesion));

        Double horas = sesion.getHoras();
        if (horas == null) {
            binding.txtHoras.setText(R.string.horas_sin_dato);
        } else {
            binding.txtHoras.setText(binding.getRoot().getResources().getQuantityString(
                    R.plurals.horas_estudio, horas == 1.0 ? 1 : 2, formatoHoras.format(horas)));
        }
    }

    private String formatearFecha(String fecha) {
        if (fecha == null || fecha.isEmpty()) {
            return "";
        }

        try {
            return fechaVisible.format(fechaGuardada.parse(fecha));
        } catch (ParseException error) {
            return fecha;
        }
    }

    @Override
    public int getItemCount() {
        return sesiones.size();
    }

    public static class SesionViewHolder extends RecyclerView.ViewHolder {

        private final ItemSesionBinding binding;

        public SesionViewHolder(ItemSesionBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
