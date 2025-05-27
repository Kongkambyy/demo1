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
