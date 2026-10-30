package com.hew.aa_firebase20251641
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.hew.aa_firebase20251641.MyApplication.Companion.auth
import com.hew.aa_firebase20251641.R
import com.hew.aa_firebase20251641.databinding.ActivityLoginBinding

// 로그인 화면 액티비티
class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 여러 버튼들에 대한 클릭이벤트 처리기 등록을 위해 함수 호출
        setClickListeners()
        // 처음 화면을 보여줌 : 하나의 액티비티에서 로긴한 상태, 로그아웃한 상태, 회원가입화면을 관리
        showInitialScreen()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    // 여러 버튼들에 대한 클릭이벤트 처리기 등록
    private fun setClickListeners() {
        binding.buttonGoSignup.setOnClickListener { showSignupScreen() } // 회원가입화면으로 이동
        binding.buttonBackLogin.setOnClickListener { showLoginScreen() } // 회원가입화면에서 로그인화면으로 이동
        binding.buttonSignup.setOnClickListener { createEmailAccount() } // 회원가입처리
        binding.buttonLogin.setOnClickListener { loginWithEmail() } // 로그인처리
        binding.buttonLogout.setOnClickListener { logout() } // 로그아웃 버튼클릭시 로그아웃 처리
    }

    // 처음 화면을 보여줌
    private fun showInitialScreen() {
        // 현재 로그인한 상태인지 체크하기 위해 인증객체(auth)로 부터 현재 사용자를 얻어옴
        val user = auth.currentUser
        // user가 null이면 사용자 인증(로그인)이 안된상태
        if (user == null) {
            showLoginScreen() // 로그인 화면으로
            return
        }

        // 로그인 인증 절차를 거친 사용자에 대해 이메일 인증이 완료되었는지 체크
        // 이메일 인증이 완료된 경우에만 메인화면(홈 화면)으로 이동
       if(!user.isEmailVerified){
           auth.signOut()
           showLoginScreen()
           // 토스트 띄우기 : toast()
           toast("사용자가 아닙니다.")
       }else
           showHomeScreen(user)

    }


    // 회원가입 : 계정을 만드는 요청 처리
    private fun createEmailAccount() {
        val email = binding.editSignupEmail.text.toString().trim()
        val password = binding.editSignupPassword.text.toString()
        val passwordConfirm = binding.editSignupPasswordConfirm.text.toString()

        // 입력한 내용의 유효성 검사
        if (!validateSignup(email, password, passwordConfirm)) return

        // 계정을 만드는 절차가 진행되므로 모든 버튼들의 상태를 비활성 상태로 전환
        setButtonsEnabled(false)
        //  firebase에 계정 생성을 요청
        val task : Task<AuthResult> = auth.createUserWithEmailAndPassword(email, password)
        task.addOnCompleteListener(object:OnCompleteListener<AuthResult>{
            override fun onComplete(p0: Task<AuthResult>) {
                //생성 실패
                if(!p0.isSuccessful){
                    setButtonsEnabled(true)
                    showError(p0.exception)
                    return
                }//if

                val user = auth.currentUser
                if(user == null){
                    setButtonsEnabled(true)
                    toast("사용자 정보를 가져오지 못함")
                    return
                }//if

                //이메일 인증 요청
                val task = user.sendEmailVerification()
                task.addOnCompleteListener {
                    if(!it.isSuccessful){
                        setButtonsEnabled(true)
                        auth.signOut()
                        toast("이메일 요청 실패")
                        return@addOnCompleteListener
                    }
                    setButtonsEnabled(true)
                    auth.signOut()
                    showLoginScreen()
                    toast("회원가입 완료. 이메일 확인 요망.")
                    binding.editLoginEmail.setText(email)
                    binding.editLoginPassword.text.clear()

                }
            }//onComplete

        })
        //showError(task.exception)
    }

    // 이메일/비번으로 회원가입 요청
    private fun loginWithEmail() {

        val email = binding.editLoginEmail.text.toString().trim()
        val password = binding.editLoginPassword.text.toString()


        // 입력한 내용의 유효성 검사
        if (email.isBlank()) {
            toast("이메일을 입력하세요.")
            return
        }
        if (password.isBlank()) {
            toast("비밀번호를 입력하세요.")
            return
        }

        setButtonsEnabled(false)

        // firebase 로그인
        val task = auth.signInWithEmailAndPassword(email, password)
        task.addOnCompleteListener {
            if(!it.isSuccessful){
                setButtonsEnabled(true)
                showError(it.exception)
                return@addOnCompleteListener
            }

            val user = auth.currentUser
            if(user == null){
                setButtonsEnabled(true)
                toast("사용자 정보를 가져오지 못함")
                return@addOnCompleteListener
            }

            //ggomsu Eddit
            /*
            if(!user.isEmailVerified){
                setButtonsEnabled(true)
                auth.signOut()
                toast("로그인 실패")
                return@addOnCompleteListener
            }*/

            //----------STR:ggomsu append
            //파이어베이스 인증 성공 --> 사용자 이메일 인증이 안된 상태
            if (!user.isEmailVerified) {
                //이메일 인증이 안되어 있는 상태이므로, 이메일 인증 요청
                user.sendEmailVerification()
                    .addOnCompleteListener { emailTask ->
                        setButtonsEnabled(true)

                        if (emailTask.isSuccessful) {
                            toast("인증 메일을 다시 전송했습니다.")
                            Log.d("FirebaseEmail", "인증 메일 요청 성공")
                        } else {
                            showError(emailTask.exception)
                            Log.e(
                                "FirebaseEmail",
                                "인증 메일 요청 실패",
                                emailTask.exception
                            )
                        }

                        auth.signOut()
                    }


                return@addOnCompleteListener
            }
            //----------END:ggomsu append

            setButtonsEnabled(true)
            toast("로그인 성공")
            showHomeScreen(user)
        }
    }

    private fun logout() {
        auth.signOut()
        showLoginScreen()
        toast("로그아웃되었습니다.")//Toast.makeText(baseContext, "로그아웃되었습니다.", Toast.LENGTH_LONG).show()
    }

    // 로그인 화면(처음화면) 보여주기
    private fun showLoginScreen() {
        binding.layoutLogin.visibility = View.VISIBLE
        binding.layoutSignup.visibility = View.GONE
        binding.layoutHome.visibility = View.GONE
        setButtonsEnabled(true)
    }

    // 회원 가입화면 보여주기
    private fun showSignupScreen() {
        binding.layoutLogin.visibility = View.GONE
        binding.layoutSignup.visibility = View.VISIBLE
        binding.layoutHome.visibility = View.GONE
        setButtonsEnabled(true)
    }

    // 홈화면(메인화면)으로 이동
    private fun showHomeScreen(user: FirebaseUser) {
        /*binding.layoutLogin.visibility = View.GONE
        binding.layoutSignup.visibility = View.GONE
        binding.layoutHome.visibility = View.VISIBLE

        binding.textUserName.text = user.displayName ?: "사용자"
        binding.textUserEmail.text = user.email ?: "이메일 정보 없음"
        binding.textProvider.text = when {
            user.providerData.any { it.providerId == GoogleAuthProvider.PROVIDER_ID } -> "로그인 방법: Google"
            user.providerData.any { it.providerId == "password" } -> "로그인 방법: 이메일(인증 완료)"
            else -> "로그인 방법: 기타"
        }
        setButtonsEnabled(true)*/
        val intent = Intent(baseContext, MainActivity::class.java)
        intent.putExtra("user", auth.currentUser?.email)
        startActivity(intent)
        setButtonsEnabled(true)
        showLoginScreen()
    }

    // 회원가입화면에서 입력한 값들의 유효성 검사
    private fun validateSignup(email: String, password: String, confirm: String): Boolean {
        if (email.isBlank()) {
            toast("이메일을 입력하세요.")
            return false
        }
        if (password.length < 6) {
            toast("비밀번호는 6자 이상이어야 합니다.")
            return false
        }
        if (password != confirm) {
            toast("비밀번호가 일치하지 않습니다.")
            return false
        }
        return true
    }

    // 버튼들의 활성화 / 비활성화 설정 : 매개변수의 값에 따라 true이면 활성화
    private fun setButtonsEnabled(enabled: Boolean) {
        binding.buttonLogin.isEnabled = enabled
        binding.buttonGoogleLogin.isEnabled = enabled
        binding.buttonGoSignup.isEnabled = enabled
        binding.buttonSignup.isEnabled = enabled
        binding.buttonBackLogin.isEnabled = enabled
        binding.buttonLogout.isEnabled = enabled
    }

    // 오류 상황에서 토스트를 띄우고 Log로 남김
    private fun showError(error: Exception?) {
        Log.e("FirebaseAuthExample", "Authentication error", error)
        toast(error?.localizedMessage ?: "인증 처리 중 오류가 발생했습니다.", true)
    }
    // 토스트 띄워줌
    private fun toast(message: String, long: Boolean = false) {
        Toast.makeText(this, message, if (long) Toast.LENGTH_LONG else Toast.LENGTH_SHORT).show()
    }
}

