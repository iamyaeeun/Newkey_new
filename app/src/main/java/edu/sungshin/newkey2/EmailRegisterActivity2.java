package edu.sungshin.newkey2;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

public class EmailRegisterActivity2 extends AppCompatActivity {
    EditText code;
    Button codeCheck,next;
    TextView codeRightText;
    private StringBuilder url;
    private SharedPreferences preferences;
    public static final String preference = "newkey";
    RequestQueue queue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_email_register2);

        code=findViewById(R.id.code);
        codeCheck=findViewById(R.id.codeCheck);
        codeRightText=findViewById(R.id.codeRightText);
        next=findViewById(R.id.next);
        preferences=getSharedPreferences(preference, Context.MODE_PRIVATE);
        queue=Volley.newRequestQueue(this);

        // 인증코드 확인 버튼
        codeCheck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                url = new StringBuilder();
                JSONObject jsonRequest = new JSONObject();

                try {
                    url.append("http://13.124.230.98:8080/user/emails/verifications");

                    String correctCode = preferences.getString("correctCode", "");
                    jsonRequest.put("correctCode", correctCode);
                    jsonRequest.put("inputCode", code.getText().toString());
                    Log.d("test", "EmailRegisterActivity2 : 입력한 인증코드 - " + code.getText().toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url.toString(), jsonRequest, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d("test", "EmailRegisterActivity2 : 응답 - " + response.toString());

                        try {
                            // 서버 응답에서 필요한 정보 추출
                            boolean isSuccess = response.getBoolean("isSuccess");

                            if (isSuccess) {
                                JSONObject result = response.getJSONObject("result");
                                boolean isCorrected = result.getBoolean("isCorrected");

                                // 인증코드 맞는 경우
                                if (isCorrected) {
                                    codeRightText.setTextColor(getResources().getColor(R.color.green));
                                    codeRightText.setText("인증이 완료되었습니다");
                                    next.setClickable(true);
                                }
                            } else {
                                next.setClickable(false);
                                Log.d("test", "인증코드 맞지 않음");
                                Toast.makeText(EmailRegisterActivity2.this, "인증코드가 맞지 않습니다", Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(EmailRegisterActivity2.this, "JSON 파싱 오류", Toast.LENGTH_SHORT).show();
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("test", "에러뜸!!" + error.toString());
                        if (error.networkResponse != null && error.networkResponse.data != null) {
                            try {
                                String errorResponse = new String(error.networkResponse.data, "utf-8");
                                JSONObject jsonObject = new JSONObject(errorResponse);
                                String errorMessage = jsonObject.getString("errorMessage");
                                // Handle BaseException
                                Log.d("test", "BaseException: " + errorMessage);
                            } catch (UnsupportedEncodingException | JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });

                request.setShouldCache(false); //이전 결과가 있어도 새로 요청하여 응답을 보여준다.
                request.setRetryPolicy(new DefaultRetryPolicy(100000000,
                        DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                        DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
                queue.add(request);
            }
        });

        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), PwRegisterActivity.class);
                startActivity(intent);
            }
        });
    }
}
