package com.example.languagetranslator;


import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.languagetranslator.databinding.FragmentManualTranslatorBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.ml.common.modeldownload.FirebaseModelDownloadConditions;
import com.google.firebase.ml.naturallanguage.FirebaseNaturalLanguage;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslateLanguage;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslator;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslatorOptions;

import java.util.ArrayList;
import java.util.Locale;


public class ManualTranslatorFragment extends Fragment {

    FragmentManualTranslatorBinding fragmentManualTranslatorBinding;
    static int fromlanguageCode = -1;
    int tolanguageCode = -1;
    String[] micLanguage ={"af_","ar_AE","bn_IN","de_","en-UK","es_","fr_","gu-IN","hi-IN","it_","ja_","kn_","ko_","mr_","ms_","ru_","ta_","te_","ur_"};
    int langlistindex;


    String inputText;
    TextToSpeech textToSpeech;
    String translatedText;
    Locale languageT2S = Locale.ENGLISH;

    public ManualTranslatorFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        fragmentManualTranslatorBinding = FragmentManualTranslatorBinding.inflate(inflater, container, false);
        fragmentManualTranslatorBinding.spinner1.setOnItemSelectedListener(new fromSpinnerClass());
        fragmentManualTranslatorBinding.spinner2.setOnItemSelectedListener(new toSpinnerClass());

        setUpSpinner();
        fragmentManualTranslatorBinding.micIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                langlistindex = fragmentManualTranslatorBinding.spinner1.getSelectedItemPosition();
                Intent i = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                i.putExtra(RecognizerIntent.EXTRA_LANGUAGE,micLanguage[langlistindex]);
                i.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak to convert into text");
                try {
                    startActivityForResult(i, 1);
                } catch (Exception e) {
                     /* Toast.makeText(ManualTranslatorFragment.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();*/
                }

            }
        });

        fragmentManualTranslatorBinding.scanNowFrame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                inputText = fragmentManualTranslatorBinding.typedText.getText().toString();
                translateText(inputText);
            }
        });
        fragmentManualTranslatorBinding.copyText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String copyscannedText = fragmentManualTranslatorBinding.translatedText.getText().toString();
                copyToClipBoard(copyscannedText);
            }
        });
        fragmentManualTranslatorBinding.txtToSpeech.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                textToSpeech = new TextToSpeech(getContext(), new TextToSpeech.OnInitListener() {
                    @Override
                    public void onInit(int i) {
                        if (i == TextToSpeech.SUCCESS) {
                            textToSpeech.setLanguage(languageT2S);
                            textToSpeech.setSpeechRate(0.9f);
                            textToSpeech.speak(translatedText, TextToSpeech.QUEUE_FLUSH, null, null);
                            Toast.makeText(getContext(), "Playing Translated Text", Toast.LENGTH_LONG).show();

                        } else {
                            Toast.makeText(getContext(), "Some Error Occurred", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

            }
        });
        return fragmentManualTranslatorBinding.getRoot();
    }
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            if (resultCode == -1 && data != null) {
                ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                fragmentManualTranslatorBinding.typedText.setText(result.get(0));
            }
        }
    }

        private void setUpSpinner() {
        ArrayAdapter adapter = ArrayAdapter.createFromResource(getContext(), R.array.languages, R.layout.spinner_dropdown_text);
        ArrayAdapter adapter1 = ArrayAdapter.createFromResource(getContext(), R.array.languages, R.layout.spinner_dropdown_text);
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_text);
        adapter1.setDropDownViewResource(R.layout.spinner_dropdown_text);
        fragmentManualTranslatorBinding.spinner1.setAdapter(adapter);
        fragmentManualTranslatorBinding.spinner2.setAdapter(adapter1);
    }
    class fromSpinnerClass implements AdapterView.OnItemSelectedListener {
        public void onItemSelected(AdapterView<?> parent, View v, int position, long id) {
            String text = parent.getItemAtPosition(position).toString();
            fromlanguageCode = getLanguageCode(text);
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {

        }
    }

    class toSpinnerClass implements AdapterView.OnItemSelectedListener {
        public void onItemSelected(AdapterView<?> parent, View v, int position, long id) {
            String text = parent.getItemAtPosition(position).toString();
            tolanguageCode = getLanguageCode(text);
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {

        }
    }

    public int getLanguageCode(String language) {
        int languageCode = 0;
        switch (language) {
            case "Afrikaans":
                languageCode = FirebaseTranslateLanguage.AF;
                languageT2S = new Locale("af_");
                break;
            case "Arabic":
                languageCode = FirebaseTranslateLanguage.AR;
                languageT2S = new Locale("ar_AE");
                break;
            case "Bengali":
                languageCode = FirebaseTranslateLanguage.BN;
                languageT2S = new Locale("bn_IN");
                break;
            case "German":
                languageCode = FirebaseTranslateLanguage.DE;
                languageT2S = new Locale("de_");
                break;
            case "English":
                languageCode = FirebaseTranslateLanguage.EN;
                languageT2S = new Locale("en_");
                break;
            case "Spanish":
                languageCode = FirebaseTranslateLanguage.ES;
                languageT2S = new Locale("es_");
                break;
            case "French":
                languageCode = FirebaseTranslateLanguage.FR;
                languageT2S = new Locale("fr_");
                break;
            case "Gujarati":
                languageCode = FirebaseTranslateLanguage.GU;
                languageT2S = new Locale("gu_IN");
                break;
            case "Hindi":
                languageCode = FirebaseTranslateLanguage.HI;
                languageT2S = new Locale("hi_IN");
                break;
            case "Italian":
                languageCode = FirebaseTranslateLanguage.IT;
                languageT2S = new Locale("it_");
                break;
            case "Japanese":
                languageCode = FirebaseTranslateLanguage.JA;
                languageT2S = new Locale("ja_");
                break;
            case "Kannada":
                languageCode = FirebaseTranslateLanguage.KN;
                languageT2S = new Locale("kn_");
                break;
            case "Korean":
                languageCode = FirebaseTranslateLanguage.KO;
                languageT2S = new Locale("ko_");
                break;
            case "Marathi":
                languageCode = FirebaseTranslateLanguage.MR;
                languageT2S = new Locale("mr_");
                break;
            case "Malay":
                languageCode = FirebaseTranslateLanguage.MS;
                languageT2S = new Locale("ms_");
                break;
            case "Russian":
                languageCode = FirebaseTranslateLanguage.RU;
                languageT2S = new Locale("ru_");
                break;
            case "Tamil":
                languageCode = FirebaseTranslateLanguage.TA;
                languageT2S = new Locale("ta_");
                break;
            case "Telugu":
                languageCode = FirebaseTranslateLanguage.TE;
                languageT2S = new Locale("te_");
                break;
            case "Urdu":
                languageCode = FirebaseTranslateLanguage.UR;
                languageT2S = new Locale("ur_");
                break;
            default:
                languageCode = 0;
        }
        return languageCode;
    }

    private void translateText(String input) {
        fragmentManualTranslatorBinding.translatingTextLable.setText("Downloading Model...");
        FirebaseTranslatorOptions options = new FirebaseTranslatorOptions.Builder()
                .setSourceLanguage(fromlanguageCode)
                .setTargetLanguage(tolanguageCode)
                .build();

        FirebaseTranslator translator = FirebaseNaturalLanguage.getInstance().getTranslator(options);
        FirebaseModelDownloadConditions conditions = new FirebaseModelDownloadConditions.Builder().build();

        fragmentManualTranslatorBinding.scanNowFrame.setVisibility(View.GONE);
        fragmentManualTranslatorBinding.translatedTextLable.setVisibility(View.GONE);
        fragmentManualTranslatorBinding.translatedText.setVisibility(View.GONE);
        fragmentManualTranslatorBinding.scannedTextLable.setVisibility(View.GONE);
        fragmentManualTranslatorBinding.typedText.setVisibility(View.GONE);
        fragmentManualTranslatorBinding.bottomLinearLayout.setVisibility(View.GONE);
        fragmentManualTranslatorBinding.scannedTextProgressBar.setVisibility(View.VISIBLE);
        fragmentManualTranslatorBinding.translatingTextLable.setVisibility(View.VISIBLE);
        translator.downloadModelIfNeeded(conditions).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                translator.translate(input)
                        .addOnSuccessListener(new OnSuccessListener<String>() {
                            @Override
                            public void onSuccess(String s) {
                                fragmentManualTranslatorBinding.translatingTextLable.setText("Translating...");
                                fragmentManualTranslatorBinding.scannedTextProgressBar.setVisibility(View.GONE);
                                fragmentManualTranslatorBinding.translatingTextLable.setVisibility(View.GONE);
                                fragmentManualTranslatorBinding.translatedText.setText(s);
                                translatedText = s;
                                fragmentManualTranslatorBinding.translatedTextLable.setVisibility(View.VISIBLE);
                                fragmentManualTranslatorBinding.translatedText.setVisibility(View.VISIBLE);
                                fragmentManualTranslatorBinding.typedText.setText(input);
                                fragmentManualTranslatorBinding.bottomLinearLayout.setVisibility(View.VISIBLE);
                                fragmentManualTranslatorBinding.scanNowFrame.setVisibility(View.VISIBLE);
                                fragmentManualTranslatorBinding.scanNowText.setText("Retranslate");
                                fragmentManualTranslatorBinding.txtToSpeech.setVisibility(View.VISIBLE);
                                fragmentManualTranslatorBinding.copyTextFrame.setVisibility(View.VISIBLE);
                                fragmentManualTranslatorBinding.scannedTextLable.setVisibility(View.VISIBLE);
                                fragmentManualTranslatorBinding.typedText.setVisibility(View.VISIBLE);

                            }
                        }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        fragmentManualTranslatorBinding.scannedTextProgressBar.setVisibility(View.GONE);
                        fragmentManualTranslatorBinding.translatingTextLable.setVisibility(View.GONE);
                        fragmentManualTranslatorBinding.bottomLinearLayout.setVisibility(View.VISIBLE);
                        fragmentManualTranslatorBinding.scanNowFrame.setVisibility(View.VISIBLE);
                        fragmentManualTranslatorBinding.scanNowText.setText("Scan Now");
                        fragmentManualTranslatorBinding.txtToSpeech.setVisibility(View.GONE);
                        fragmentManualTranslatorBinding.copyTextFrame.setVisibility(View.GONE);
                        Toast.makeText(getContext(), "Fail to Translate" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                fragmentManualTranslatorBinding.scannedTextProgressBar.setVisibility(View.GONE);
                fragmentManualTranslatorBinding.translatingTextLable.setVisibility(View.GONE);
                fragmentManualTranslatorBinding.bottomLinearLayout.setVisibility(View.VISIBLE);
                fragmentManualTranslatorBinding.scanNowFrame.setVisibility(View.VISIBLE);
                fragmentManualTranslatorBinding.scanNowText.setText("Scan Now");
                fragmentManualTranslatorBinding.txtToSpeech.setVisibility(View.GONE);
                fragmentManualTranslatorBinding.copyTextFrame.setVisibility(View.GONE);
                Toast.makeText(getContext(), "Fail to Download Language Model" + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void copyToClipBoard(String text) {
        ClipboardManager clipboardManager = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clipData = ClipData.newPlainText("Copied data", text);
        clipboardManager.setPrimaryClip(clipData);
        Toast.makeText(getActivity().getApplicationContext(), "Copied to Clipboard", Toast.LENGTH_SHORT).show();
    }

}