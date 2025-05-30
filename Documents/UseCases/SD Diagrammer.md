# Sekvensdiagrammer for alle Use Cases

## Admin bruger

```mermaid
sequenceDiagram
participant Bruger
participant System
participant Database

Bruger->>System: Logger ind
System->>Database: Verificer loginoplysninger
Database-->>System: Returner brugerrolle (admin eller alm. bruger)

alt Bruger er admin
    System->>Bruger: Vis admin-dashboard
    Bruger->>System: Administrer brugere/data/promoveringer
    System->>Database: Gem ændringer og log handlinger
    Database-->>System: Bekræft gemt data
    System-->>Bruger: Bekræft handling
else Bruger er almindelig bruger
    System->>Bruger: Vis almindelig brugerprofil
end
```

## Se profil

```mermaid
sequenceDiagram
participant Bruger
participant System
participant Database

Bruger->>System: Vælg "se profil"
System->>Database: Hent profildata (navn, email, oprettelsesdato, historik)

alt Database aktiv og session gyldig
    Database-->>System: Returner profildata
    System-->>Bruger: Vis profildata
else Database ikke aktiv
    System-->>Bruger: Vis fejlmeddelelse (databasefejl)
else Session udløbet
    System-->>Bruger: Returner til login-side
end
```

## Rediger profil

```mermaid
sequenceDiagram
participant Bruger
participant System
participant Database

Bruger->>System: Vælg "rediger profil"
System->>Database: Hent eksisterende profildata

alt Database aktiv og session gyldig
    Database-->>System: Returner eksisterende profildata
    System-->>Bruger: Vis redigeringsside med eksisterende data
    Bruger->>System: Indtast ændringer (navn, email, password)
    System->>Database: Opdater profildata
    Database-->>System: Bekræft opdatering
    System-->>Bruger: Bekræft ændringer gemt
else Database ikke aktiv
    System-->>Bruger: Vis fejlmeddelelse (databasefejl)
else Session udløbet
    System-->>Bruger: Returner til login-side
end
```

## Slet profil

```mermaid
sequenceDiagram
participant Bruger
participant System
participant Database

Bruger->>System: Vælg "slet profil"
System-->>Bruger: Vis bekræftelsesknap
Bruger->>System: Bekræft sletning
System-->>Bruger: Bed om kodeord
Bruger->>System: Indtast kodeord
System->>Database: Bekræft kodeord

alt Kodeord korrekt, aktiv session og DB
    System->>Database: Slet profil og data permanent
    Database-->>System: Bekræft sletning
    System-->>Bruger: Bekræft profil slettet
else Database ikke aktiv
    System-->>Bruger: Vis fejlmeddelelse (databasefejl)
else Session udløbet
    System-->>Bruger: Returner til login-side
end
```

## Profil Reviews

```mermaid
sequenceDiagram
participant Bruger
participant System
participant Database

Bruger->>System: Vælg "profil reviews"
System->>Database: Hent reviews fra andre brugere

alt Database aktiv og session gyldig
    Database-->>System: Returner reviews
    System-->>Bruger: Vis reviews
    Bruger->>System: Appeller review (valgfrit)
    System->>Database: Gem appel
    Database-->>System: Bekræft appel modtaget
    System-->>Bruger: Bekræft appel sendt
else Database ikke aktiv
    System-->>Bruger: Vis fejlmeddelelse (databasefejl)
else Session udløbet
    System-->>Bruger: Returner til login-side
end
```

## Køber Sælger Chat

```mermaid
sequenceDiagram
participant Køber
participant System
participant Database
participant Sælger

Køber->>System: Start chat med sælger (neutral eller vare-specifik)
System->>Database: Opret eller hent chat-session
Database-->>System: Returner chat-session
System->>Sælger: Notificer sælger (hvis online)

Køber->>System: Skriv besked
System->>Database: Gem besked
System->>Sælger: Vis besked i realtid (hvis online)

alt Sælger offline
    System->>Sælger: Send email-notifikation
end
```

## Oprettelse af bruger

```mermaid
sequenceDiagram
participant NyBruger
participant System
participant Database

NyBruger->>System: Indsend registreringsformular
System->>Database: Tjek eksisterende email og adgangskode

alt Email/adgangskode findes allerede
    Database-->>System: Returner fejl
    System-->>NyBruger: Vis fejlmeddelelse
else Oplysninger gyldige
    Database-->>System: Bekræft ledig
    System->>Database: Gem brugerprofil
    Database-->>System: Bekræft oprettelse
    System-->>NyBruger: Bekræft oprettelse gennemført
end
```

## Handel Rating

```mermaid
sequenceDiagram
participant Køber
participant System
participant Database

Køber->>System: Afgiv stjerne-rating
System->>Database: Gem rating på sælgers profil
Database-->>System: Bekræft rating gemt
System-->>Køber: Bekræft rating modtaget
```

## Handel Feedback

```mermaid
sequenceDiagram
participant Køber
participant System
participant Database
participant Sælger

Køber->>System: Skriv feedback på afsluttet handel
System->>Database: Gem feedback på sælgers profil
Database-->>System: Bekræft feedback gemt
System->>Sælger: Notificer sælger om ny feedback

Sælger->>System: Appeller feedback (valgfrit)
System->>Database: Gem appel
Database-->>System: Bekræft appel modtaget
System-->>Sælger: Bekræft appel sendt
```
