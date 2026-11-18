package com.example.lab6;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.firebase.ui.auth.AuthMethodPickerLayout;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract;
import com.google.firebase.auth.FirebaseAuth;
import com.example.lab6.databinding.CustomLoginBinding;

import java.util.Arrays;

public class CustomLoginActivity extends AppCompatActivity {

    private CustomLoginBinding binding;

    private final ActivityResultLauncher<Intent> loginLauncher =
            registerForActivityResult(new FirebaseAuthUIActivityResultContract(), result -> {
                binding.btnLoginMail.setEnabled(true);
                binding.btnLoginGoogle.setEnabled(true);
                if (result.getResultCode() == RESULT_OK
                        && FirebaseAuth.getInstance().getCurrentUser() != null) {
                    abrirPrincipal();
                    return;
                }

                if (result.getIdpResponse() != null) {
                    Log.e("FirebaseAuth", "Error de acceso", result.getIdpResponse().getError());
                    Toast.makeText(this, R.string.error_acceso, Toast.LENGTH_LONG).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = CustomLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (vista, insets) -> {
            Insets barras = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            vista.setPadding(barras.left, barras.top, barras.right, barras.bottom);
            return insets;
        });

        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            abrirPrincipal();
            return;
        }

        binding.btnLoginMail.setOnClickListener(view -> mostrarAcceso(new AuthUI.IdpConfig.EmailBuilder().build()));
        binding.btnLoginGoogle.setOnClickListener(view -> mostrarAcceso(new AuthUI.IdpConfig.GoogleBuilder().build()));
        if (savedInstanceState == null) {
            mostrarAcceso(null);
        }
    }

    private void mostrarAcceso(AuthUI.IdpConfig proveedorPreferido) {
        binding.btnLoginMail.setEnabled(false);
        binding.btnLoginGoogle.setEnabled(false);

        AuthMethodPickerLayout selector =
                new AuthMethodPickerLayout.Builder(R.layout.custom_login)
                        .setEmailButtonId(R.id.btn_login_mail)
                        .setGoogleButtonId(R.id.btn_login_google)
                        .build();

        AuthUI.SignInIntentBuilder acceso = AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(Arrays.asList(
                        new AuthUI.IdpConfig.EmailBuilder().build(),
                        new AuthUI.IdpConfig.GoogleBuilder().build()))
                .setTheme(R.style.Theme_LAB6)
                .setLogo(R.drawable.logo_estudio)
                .setCredentialManagerEnabled(false)
                .setAuthMethodPickerLayout(selector);

        if (proveedorPreferido != null) {
            acceso.setDefaultProvider(proveedorPreferido);
        }
        loginLauncher.launch(acceso.build());
    }

    private void abrirPrincipal() {
        startActivity(new Intent(this, MainActivity.class)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
        finish();
    }
}
