package ru.noties.parcelable;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import ru.noties.parcelable.obj.SomeAnnotatedParcelable;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final SomeAnnotatedParcelable object = new SomeAnnotatedParcelable();

        final Bundle bundle = new Bundle();
        bundle.putParcelable("", (android.os.Parcelable) object);

        final SomeAnnotatedParcelable other = bundle.getParcelable("");
    }
}
