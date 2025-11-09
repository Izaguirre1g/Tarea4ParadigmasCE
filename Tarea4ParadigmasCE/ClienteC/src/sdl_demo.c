// Ejemplo mínimo compatible con SDL3 y SDL2
#ifdef USE_SDL2
  #include <SDL.h>
#else
  #include <SDL3/SDL.h>
#endif

#include <stdio.h>
#include <stdbool.h>
#include <string.h>

int main(int argc, char **argv) {
#ifdef USE_SDL2
    if (SDL_Init(SDL_INIT_VIDEO) != 0) {
        fprintf(stderr, "No se pudo inicializar SDL2: %s\n", SDL_GetError());
        return 1;
    }

    SDL_Window *window = SDL_CreateWindow("Cliente SDL", SDL_WINDOWPOS_CENTERED, SDL_WINDOWPOS_CENTERED, 640, 480, 0);
    if (!window) {
        fprintf(stderr, "No se pudo crear la ventana SDL2: %s\n", SDL_GetError());
        SDL_Quit();
        return 1;
    }

    SDL_Renderer *renderer = SDL_CreateRenderer(window, -1, 0);
    if (!renderer) {
        fprintf(stderr, "No se pudo crear el renderer SDL2: %s\n", SDL_GetError());
        SDL_DestroyWindow(window);
        SDL_Quit();
        return 1;
    }

    bool running = true;
    while (running) {
        SDL_Event e;
        while (SDL_PollEvent(&e)) {
            if (e.type == SDL_QUIT) running = false;
            if (e.type == SDL_KEYDOWN) {
                if (e.key.keysym.sym == SDLK_ESCAPE) running = false;
            }
        }

        SDL_SetRenderDrawColor(renderer, 30, 144, 255, 255);
        SDL_RenderClear(renderer);
        SDL_RenderPresent(renderer);
        SDL_Delay(16);
    }

    SDL_DestroyRenderer(renderer);
    SDL_DestroyWindow(window);
    SDL_Quit();
    return 0;
#else
    // Diagnostic prints before SDL_Init
    // SDL3 changed version API; avoid SDL_version usage here to remain compatible.
    int numDrivers = SDL_GetNumVideoDrivers();
    printf("Number of compiled-in video drivers: %d\n", numDrivers);
    for (int i = 0; i < numDrivers; ++i) {
        const char *drv = SDL_GetVideoDriver(i);
        printf("  driver[%d] = %s\n", i, drv ? drv : "(null)");
    }

    if (SDL_Init(SDL_INIT_VIDEO) != 0) {
        const char *err = SDL_GetError();
        int len = err ? (int)strlen(err) : 0;
        fprintf(stderr, "SDL_Init failed: '%s' (len=%d)\n", err ? err : "(null)", len);
        return 1;
    }

    // SDL3: SDL_CreateWindow(title, w, h, flags)
    SDL_Window *window = SDL_CreateWindow("Cliente SDL3", 640, 480, 0);
    if (!window) {
        fprintf(stderr, "No se pudo crear la ventana SDL3: %s\n", SDL_GetError());
        SDL_Quit();
        return 1;
    }

    // SDL3: SDL_CreateRenderer(window, name)
    SDL_Renderer *renderer = SDL_CreateRenderer(window, NULL);
    if (!renderer) {
        fprintf(stderr, "No se pudo crear el renderer SDL3: %s\n", SDL_GetError());
        SDL_DestroyWindow(window);
        SDL_Quit();
        return 1;
    }

    bool running = true;
    while (running) {
        SDL_Event e;
        while (SDL_PollEvent(&e)) {
            if (e.type == SDL_EVENT_QUIT) running = false;
            // No usamos e.key.keysym en SDL3: usaremos el estado del teclado
        }

        // Obtener estado del teclado y castear a tipo compatible
        const void *kb = SDL_GetKeyboardState(NULL);
        const Uint8 *state = (const Uint8 *)kb;
        if (state && state[SDL_SCANCODE_ESCAPE]) running = false;

        SDL_SetRenderDrawColor(renderer, 30, 144, 255, 255); // DodgerBlue
        SDL_RenderClear(renderer);
        SDL_RenderPresent(renderer);
        SDL_Delay(16);
    }

    SDL_DestroyRenderer(renderer);
    SDL_DestroyWindow(window);
    SDL_Quit();
    return 0;
#endif
}
