

enum  TAG{USERNAME, PASSWORD, REGUSERNAME, REGEMAIL, REGPASSWORD, REGPASSWORD2};

extern bool g_showdialog;
extern int g_stage;

void UpdateLogo();

void Click_Login();
void Click_GoToRegister();
void Click_Register();
void Click_GoToLogin();
void Click_GoToCredits();
void Click_GoToStory();
void Click_GoToOnline();
void Click_GoToMain();
void Click_Retry();
void Click_SwitchView();

void CloseDialog();
extern void (*DialogContinue)();
void Click_DialogContinue();
void Click_DontShow();
void Dialog(const char* msg, void (*Continue)());

void Click_EquipNext();
void Click_Shoot();
void Unclick_Shoot();
void Click_Action();
void Click_Reload();
void Click_Jump();
void Unclick_Jump();
void Click_Crouch();
void Click_Run();

void Rotational(float dx, float dy);
void Movement(float dx, float dy);
void Forward();
void Unforward();
void Back();
void Unback();
void Left();
void Unleft();
void Right();
void Unright();
void RedoHP();
void RedoStamina();
void RedoAmmo();
void RedoScore();
void UpdateGUI();

void Error(const char* msg);
void Status(const char* status);
void StatusBack(const char* status);
void Chat(const char* chat);

void Keymap();