package com.example.lab6;

import androidx.lifecycle.ViewModel;

import com.google.android.gms.tasks.Task;

// Conserva el guardado en curso cuando Android recrea la pantalla por una rotación.
public class FormularioSesionViewModel extends ViewModel {
    public Task<Void> guardado;
}
