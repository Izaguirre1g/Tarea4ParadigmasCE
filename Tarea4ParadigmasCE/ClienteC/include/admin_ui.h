#ifndef ADMIN_UI_H
#define ADMIN_UI_H

#define SDL_MAIN_HANDLED
#include <SDL2/SDL.h>
#include <SDL2/SDL_ttf.h>

typedef struct {
    SDL_Window* win;
    SDL_Renderer* ren;
    TTF_Font* font;
} AdminUI;

/* Dropdown de jugadores */
typedef struct {
    SDL_Rect box;
    int isOpen;
    int selectedIndex;
    int count;
    int ids[32];
    char names[32][32];
} DropDown;

/* Botón simple */
typedef struct {
    SDL_Rect rect;
    const char* label;
    int id;  // ID único para identificar qué botón se presionó
} Button;

/* Campo de texto para input */
typedef struct {
    SDL_Rect rect;
    char text[16];
    int maxLen;
    int isActive;
    const char* label;
} InputField;

/* Radio button para selección */
typedef struct {
    SDL_Rect rect;
    const char* label;
    int isSelected;
} RadioButton;

/* Grupo de radio buttons (solo uno puede estar seleccionado) */
typedef struct {
    RadioButton* buttons;
    int count;
    int selectedIndex;
} RadioGroup;

/* Estado global de la UI */
typedef struct {
    // Dropdown de jugadores
    DropDown playerDropdown;

    // Sección COCODRILOS
    RadioGroup crocTypeGroup;  // Rojo o Azul
    InputField crocLiana;      // 0-8
    InputField crocAltura;     // 0-540
    Button btnCrearCroc;
    char crocLianaRangeText[64];  // Texto informativo del rango de la liana

    // Sección FRUTAS
    RadioGroup fruitTypeGroup; // Banana, Naranja, Cereza
    InputField fruitLiana;     // 0-8
    InputField fruitAltura;    // 0-540
    InputField fruitPuntos;    // 10-100
    Button btnCrearFruta;
    char fruitLianaRangeText[64]; // Texto informativo del rango de la liana

    // Sección ELIMINAR FRUTA
    InputField delFruitLiana;  // 0-8
    InputField delFruitAltura; // 0-540
    Button btnEliminarFruta;
    char delFruitLianaRangeText[64]; // Texto informativo del rango de la liana

    // Botón actualizar lista
    Button btnActualizarLista;

    // Campo de input activo
    InputField* activeInput;
} AdminUIState;

/* Funciones principales */
int admin_ui_init(AdminUI* ui);
void admin_ui_shutdown(AdminUI* ui);
void admin_ui_render(AdminUI* ui, AdminUIState* state);
void admin_ui_handle_click(AdminUIState* state, int x, int y, int sock);
void admin_ui_handle_keypress(AdminUIState* state, SDL_Event* e);
void admin_ui_update_players(const char* json);
void admin_ui_run(int sock);

/* Funciones auxiliares */
void draw_text(SDL_Renderer* ren, TTF_Font* font, const char* text, int x, int y, SDL_Color color);
void draw_button(SDL_Renderer* ren, TTF_Font* font, const Button* btn);
void draw_input_field(SDL_Renderer* ren, TTF_Font* font, const InputField* field);
void draw_input_field_with_label(SDL_Renderer* ren, TTF_Font* font, const InputField* field, const char* customLabel);
void draw_radio_button(SDL_Renderer* ren, TTF_Font* font, const RadioButton* radio);
void draw_section_title(SDL_Renderer* ren, TTF_Font* font, const char* title, int x, int y);

#endif // ADMIN_UI_H