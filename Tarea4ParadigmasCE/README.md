# 🎮 DonCEy Kong Jr. - Multiplayer Game

<div align="center">

![Version](https://img.shields.io/badge/version-2.0-blue.svg)
![Java](https://img.shields.io/badge/Java-17+-orange.svg)
![C](https://img.shields.io/badge/C-99-blue.svg)
![SDL2](https://img.shields.io/badge/SDL2-2.0+-green.svg)
![License](https://img.shields.io/badge/license-MIT-green.svg)

**Juego multiplayer basado en Donkey Kong Jr. con arquitectura cliente-servidor**

[Características](#características) • [Instalación](#instalación) • [Uso](#uso) • [Patrones de Diseño](#patrones-de-diseño)

</div>

---

## 📋 Tabla de Contenidos

- [Características](#características)
- [Requisitos](#requisitos)
- [Instalación](#instalación)
- [Compilación y Ejecución](#compilación-y-ejecución)
- [Controles del Juego](#controles-del-juego)
- [Panel de Administrador](#panel-de-administrador)
- [Arquitectura](#arquitectura)
- [Patrones de Diseño](#patrones-de-diseño)
- [Mejoras Recientes](#mejoras-recientes)

---

## ✨ Características

### 🎮 Jugabilidad
- ✅ **9 lianas** para trepar y explorar
- ✅ **Animación suave** del personaje (6 frames)
- ✅ **Enemigos inteligentes**: Cocodrilos rojos (oscilan) y azules (caen)
- ✅ **Sistema de puntuación** con vidas extra cada 500 puntos
- ✅ **Física realista**: Gravedad, saltos, colisiones
- ✅ **Invencibilidad temporal** tras perder una vida

### 🌐 Multijugador
- ✅ **Partidas independientes** por jugador
- ✅ **Modo Espectador** para ver partidas en curso
- ✅ **Panel de Administrador** para control en tiempo real
- ✅ **Servidor escalable** con hilos independientes

### 🎨 Visual
- ✅ **Sprites originales** de alta calidad
- ✅ **Animaciones fluidas** para todos los personajes
- ✅ **HUD informativo** con puntuación y vidas
- ✅ **Efectos visuales** para eventos importantes

---

## 💻 Requisitos

### Servidor (Java)
- **Java JDK** 17 o superior
- **Gson** 2.10.1 (incluido en `src/`)
- Sistema operativo: Windows, Linux, macOS

### Cliente (C + SDL)
- **Compilador C**: GCC (MinGW en Windows)
- **CMake** 3.15 o superior
- **SDL2** o **SDL3**
- **SDL2_image**
- **SDL2_ttf**

---

## 🔧 Instalación

### Windows (MSYS2 - Recomendado)

1. **Instalar MSYS2** desde [https://www.msys2.org/](https://www.msys2.org/)

2. **Abrir terminal MSYS2 MinGW 64-bit** y ejecutar:

```bash
# Actualizar sistema
pacman -Syu

# Instalar herramientas de compilación
pacman -S mingw-w64-x86_64-gcc mingw-w64-x86_64-cmake mingw-w64-x86_64-ninja

# Instalar SDL2 y dependencias
pacman -S mingw-w64-x86_64-SDL2 mingw-w64-x86_64-SDL2_image mingw-w64-x86_64-SDL2_ttf
```

3. **Clonar/descargar el proyecto**

---

## 🚀 Compilación y Ejecución

### Método Rápido (Scripts Automáticos)

#### 🖥️ Servidor

**Opción 1: Script Automático (Windows)**
```batch
compilar_servidor.bat
```

**Opción 2: Manual**
```bash
cd src
javac -d ../production/Tarea4ParadigmasCE -cp ".;gson-2.10.1.jar" entities/*.java model/*.java patterns/*/*.java network/*.java server/*.java utils/*.java

cd ../production/Tarea4ParadigmasCE
java -cp ".;../../src/gson-2.10.1.jar" server.GameServer
```

#### 🎮 Cliente

**Opción 1: Script Automático (Windows)**
```batch
compilar_cliente.bat
```

**Opción 2: Manual (MSYS2)**
```bash
cd ClienteC

# Primera compilación
rm -rf build && mkdir build
cd build
cmake ..
cmake --build . --target dkj_unified

# Ejecutar
cd bin
./dkj_unified.exe
```

**Opción 3: Ejecución Rápida (después de compilar)**
```bash
cd ClienteC/build/bin
./dkj_unified.exe
```

---

## 🎮 Controles del Juego

### Jugador

| Tecla | Acción |
|-------|--------|
| `←` | Mover izquierda |
| `→` | Mover derecha |
| `↑` | Subir en liana |
| `↓` | Bajar en liana |
| `ESPACIO` | Saltar |
| `ESC` | Salir |

### Menú Principal

Al ejecutar el cliente, selecciona el modo:
- **JUGAR**: Iniciar partida
- **ESPECTADOR**: Ver partidas en curso
- **ADMINISTRADOR**: Panel de control

---

## 🛠️ Panel de Administrador

### Funcionalidades Disponibles

#### 1. Seleccionar Jugador
- Click en **ACTUALIZAR LISTA** para ver jugadores activos
- Seleccionar jugador del dropdown

#### 2. Crear Cocodrilo
- **Tipo**: Rojo (oscila) o Azul (cae)
- **Liana**: 1-9
- **Altura**: 0-540 píxeles
- Click en **CREAR COCODRILO**

#### 3. Crear Fruta
- **Tipo**: Banana (70pts), Naranja (100pts), Cereza (50pts)
- **Liana**: 1-9
- **Altura**: 0-540 píxeles
- **Puntos**: 10-100 (personalizable)
- Click en **CREAR FRUTA**

#### 4. Eliminar Fruta
- **Liana**: 1-9
- **Altura**: 0-540 píxeles
- Click en **ELIMINAR FRUTA**

### Comandos de Consola (Avanzado)

```
ADMIN PLAYERS                           # Lista de jugadores
ADMIN SELECT <id>                        # Seleccionar jugador
ADMIN CROC <ROJO|AZUL> <liana> <altura>  # Crear cocodrilo
ADMIN FRUIT <tipo> <liana> <altura> <pts> # Crear fruta
ADMIN DELFRUIT <liana> <altura>          # Eliminar fruta
ADMIN SPEED <multiplicador>              # Velocidad de enemigos
ADMIN LIVES <cantidad>                   # Modificar vidas
ADMIN SCORE <cantidad>                   # Modificar puntuación
```

---

## 🏗️ Arquitectura

### Servidor (Java)

```
GameServer (puerto 5000)
    ↓
ClientHandler (1 hilo por cliente)
    ↓
PlayerRegistry (registro global)
    ↓
GameManager (1 por jugador)
    ↓
GameState (estado encapsulado)
```

### Cliente (C + SDL)

```
Launcher (menú de selección)
    ├── Cliente Jugador
    ├── Cliente Espectador
    └── Cliente Administrador
         ↓
    NetworkLayer (TCP)
         ↓
    RenderEngine (SDL2)
```

---

## 🎯 Patrones de Diseño

### 1. Factory Pattern (Abstract Factory)
**Ubicación**: `patterns/factory/`

Crea entidades del juego de forma estandarizada:
```java
GameObjectFactory factory = new GameObjectFactoryImpl();
Cocodrilo rojo = factory.crearCocodrilo(TipoCocodrilo.ROJO, posicion);
Fruta banana = factory.crearFruta(TipoFruta.BANANA, posicion);
```

### 2. Strategy Pattern
**Ubicación**: `patterns/strategy/`

Comportamientos de movimiento intercambiables:
```java
// Cocodrilo rojo: movimiento oscilante
cocodriloRojo.setStrategy(new RedCrocStrategy());

// Cocodrilo azul: caída libre
cocodriloAzul.setStrategy(new BlueCrocStrategy());
```

### 3. Observer Pattern
**Ubicación**: `patterns/observer/`

Notificación de cambios de estado:
```java
// GameManager notifica a todos los clientes conectados
observable.notificarObservadores(gameStateString);

// ClientHandler recibe actualizaciones
@Override
public void actualizar(Object mensaje) {
    out.println((String) mensaje);
}
```

---

## 🆕 Mejoras Recientes

### ✨ Version 2.0 (25 Nov 2025)

#### 1. Sistema de Animación para DK Jr.
- 🎬 **6 frames de animación** (jr1, jr2, jr4, jr5, jr6, jr7)
- 🔄 **Ciclo suave** a ~7.5 FPS
- 🎨 **Fallback automático** a sprite estático
- 💾 **Sin fugas de memoria**

#### 2. Soporte para 9 Lianas
- 🎮 **9 lianas completas** (antes solo 6)
- 🛠️ **Panel admin actualizado**
- ✅ **Validación coherente** cliente-servidor
- 📈 **Escalable** para futuras expansiones

**Ver detalles completos en**: [MEJORAS_IMPLEMENTADAS.md](MEJORAS_IMPLEMENTADAS.md)

---

## 📁 Estructura del Proyecto

```
Tarea4ParadigmasCE/
├── src/                      # Servidor Java
│   ├── entities/            # Entidades del juego
│   ├── model/               # Modelo de datos
│   ├── network/             # Comunicación
│   ├── server/              # Lógica del servidor
│   ├── patterns/            # Patrones de diseño
│   │   ├── factory/
│   │   ├── observer/
│   │   └── strategy/
│   └── utils/               # Utilidades
│
├── ClienteC/                # Cliente C
│   ├── include/             # Headers
│   ├── src/                 # Código fuente
│   ├── assets/              # Sprites y fuentes
│   └── build/               # Compilación
│
├── compilar_servidor.bat    # Script compilación servidor
├── compilar_cliente.bat     # Script compilación cliente
├── REVISION_PROYECTO.md     # Revisión técnica completa
└── MEJORAS_IMPLEMENTADAS.md # Documentación de mejoras

```

---

## 🐛 Solución de Problemas

### Error: "No se puede conectar al servidor"
- ✅ Verifica que el servidor esté ejecutándose
- ✅ Confirma que el puerto 5000 no esté en uso
- ✅ Revisa firewall de Windows

### Error: "SDL2 not found"
- ✅ Reinstala SDL2 con MSYS2: `pacman -S mingw-w64-x86_64-SDL2`
- ✅ Verifica que estés usando **MSYS2 MinGW 64-bit** (no MSYS2 MSYS)

### Error: "Cannot find font"
- ✅ Verifica que `ClienteC/assets/arial.ttf` exista
- ✅ Ejecuta desde el directorio correcto

### Animación no se muestra
- ✅ Verifica que existan: `jr1.png`, `jr2.png`, `jr4.png`, `jr5.png`, `jr6.png`, `jr7.png` en `ClienteC/assets/`
- ✅ Revisa la consola de inicio para ver qué sprites se cargaron

---

## 📝 Licencia

Este proyecto es de código abierto y está disponible bajo la licencia MIT.

---

## 👥 Contribuciones

Las contribuciones son bienvenidas. Por favor:
1. Fork el proyecto
2. Crea una rama de feature (`git checkout -b feature/AmazingFeature`)
3. Commit tus cambios (`git commit -m 'Add some AmazingFeature'`)
4. Push a la rama (`git push origin feature/AmazingFeature`)
5. Abre un Pull Request

---

## 📞 Soporte

¿Problemas o preguntas? Consulta:
- 📖 [REVISION_PROYECTO.md](REVISION_PROYECTO.md) - Documentación técnica completa
- 🆕 [MEJORAS_IMPLEMENTADAS.md](MEJORAS_IMPLEMENTADAS.md) - Últimas mejoras
- 📂 Issues de GitHub (si aplica)

---

<div align="center">

**¡Disfruta del juego! 🎮🐵**

Hecho con ❤️ usando Java, C y SDL2

</div>

