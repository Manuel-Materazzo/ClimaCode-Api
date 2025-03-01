# ClimaCode: Weather Nowcasting and Forecasting API

ClimaCode-Api is a Spring Boot-based REST API designed to provide weather nowcasts and forecasts by integrating data from various weather radar sources and third-party weather forecast providers.  It leverages weather radar tiles, web scraping techniques, and provides endpoints for accessing both raw and processed weather data.
The API is designed to provide accurate and timely weather information for specific geographical locations.

## Features

- **Weather Forecasting**:
   - Retrieve raw weather forecasts for a given location.
   - Match weather forecasts with specific weather conditions (e.g., rain, snow, storms) for easy alert systems implementation.
   - Integrates API services or web scraping as forecasting sources.

- **Weather Nowcasting**:
   - Retrieve real-time weather nowcasts using radar imagery.
   - Match nowcasts with specific weather types for easy alert systems implementation..
   - Fetch radar imagery for visualization from third party APIs.

- **OpenAPI Documentation**:
   - Swagger UI is available for API testing and exploration.

- **Configuration**:
   - Highly customizable weather sources through `application.yml`.
   - Supports multiple weather radars and web scraping configurations.

---

## Getting Started

### Prerequisites

- Java 21
- Maven 3.6+
- A weather radar API key (if required by the configured radar sources).

### Local Installation

1. Clone the repository:
   ```bash
   git clone https://github.com/your-repo/ClimaCode-Api.git
   cd ClimaCode-Api
   ```

2. Build the project:
   ```bash
   mvn clean install
   ```

3. Run the application:
   ```bash
   mvn spring-boot:run
   ```

4. The API will be available at: `http://localhost:8080/api`.

---
## Configuration

The `application.yml` file is used to configure weather sources, web scrapers, and other application settings.

Please refer to the documentation or read comments on the `application.yml` file for details on how to configure additional weather radars and scrapers.

---

## Usage

Please refer to the API documentation for details of each endpoint.

Once the application is running, you can find the interactive OpenApi Documentation at:
- Swagger UI: [http://localhost:8080/api/swagger](http://localhost:8080/api/swagger)
- API Docs: [http://localhost:8080/api/api-docs](http://localhost:8080/api/api-docs)

### Contributing
Contributions are welcome! Please fork the repository and submit a pull request  with detailed descriptions of your changes.  

### License
This project is licensed under the MIT License.