# Pakowanie aplikacji krok po kroku

## 1. Tworzenie pliku JAR za pomocą Gradle

1. Otwórz terminal w głównym katalogu projektu, gdzie znajduje się plik `gradlew`
2. Wykonaj polecenie:
   ```
   ./gradlew bootJar
   ```
   (lub na Windows: `gradlew.bat bootJar`)
3. Po zakończeniu procesu, plik JAR zostanie utworzony w katalogu `build/libs/`
4. Sprawdź, czy plik JAR został poprawnie wygenerowany

## 2. Utworzenie pliku EXE z JAR za pomocą Launch4j

1. Upewnij się, że masz zainstalowany Launch4j na swoim komputerze
2. Znajdź swój plik konfiguracyjny `config.xml`
3. Otwórz plik `config.xml` w edytorze tekstu i zaktualizuj następujące ścieżki:
    - Ścieżkę do wejściowego pliku JAR:
      ```xml
      <jar>ścieżka/do/twojego/pliku.jar</jar>
      ```
    - Ścieżkę do wyjściowego pliku EXE:
      ```xml
      <outfile>ścieżka/do/wyjściowego/pliku.exe</outfile>
      ```
    - Ścieżkę do ikony aplikacji:
      ```xml
      <icon>ścieżka/do/ikony.ico</icon>
      ```
4. Zapisz zmiany w pliku `config.xml`
5. Uruchom Launch4j:
   ```
   launch4j config.xml
   ```
6. Poczekaj, aż Launch4j zakończy konwersję JAR do EXE
7. Sprawdź, czy plik EXE został poprawnie wygenerowany w lokalizacji, którą określiłeś w pliku konfiguracyjnym

## 3. Budowanie frontendu

1. Przejdź do folderu frontendu:
   ```
   cd ścieżka/do/folderu/frontend
   ```
2. Upewnij się, że masz zainstalowany Node.js i npm
3. Zainstaluj wszystkie wymagane zależności (jeśli jeszcze tego nie zrobiłeś):
   ```
   npm install
   ```
4. Uruchom budowanie aplikacji Electron dla systemu Windows:
   ```
   npm run electron:build:win
   ```
5. Poczekaj na zakończenie procesu budowania
6. Pliki wynikowe znajdziesz w katalogu `dist` (lub innym, w zależności od konfiguracji projektu)

## 4. Dodanie pliku instalacyjnego `mariadb-installer.msi` do folderu tools (potrzebna zmiana nazwy)

link do pobrania [mariadb-installer.msi](https://mariadb.org/download/?t=mariadb&p=mariadb&r=11.7.2&os=windows&cpu=x86_64&pkg=msi&mirror=icm)

## 5. Tworzenie instalatora za pomocą Inno Setup

1. Upewnij się, że masz zainstalowany Inno Setup na swoim komputerze
2. Znajdź swój plik konfiguracyjny `installer.iss`
3. Uruchom kompilację skryptu instalatora:
   ```
   iscc installer.iss
   ```
   lub otwórz plik `installer.iss` w Inno Setup i kliknij przycisk "Compile"
4. Poczekaj na zakończenie procesu kompilacji
5. Instalator zostanie utworzony w katalogu określonym w pliku `installer.iss` (zazwyczaj w sekcji `OutputDir`)

## Uwagi dodatkowe

- Upewnij się, że wszystkie ścieżki w plikach konfiguracyjnych są poprawne i aktualne
- Jeśli kompilacja któregokolwiek z etapów zakończy się niepowodzeniem, sprawdź logi błędów
- Przed dystrybucją aplikacji, przetestuj instalator na czystym systemie, aby upewnić się, że działa poprawnie
- Pamiętaj o dołączeniu wszelkich niezbędnych plików konfiguracyjnych, bibliotek i zależności
