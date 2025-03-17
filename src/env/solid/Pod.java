package solid;

import cartago.Artifact;
import cartago.OPERATION;
import cartago.OpFeedbackParam;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

/**
 * A CArtAgO artifact that agent can use to interact with LDP containers in a Solid pod.
 */
public class Pod extends Artifact {

    private String podURL; // the location of the Solid pod

  /**
   * Method called by CArtAgO to initialize the artifact.
   *
   * @param podURL The location of a Solid pod
   */
    public void init(String podURL) {
        this.podURL = podURL;
        log("Pod artifact initialized for: " + this.podURL);
    }



    /*
        Task 2.1 Creating an LDP container
        Implement the method createContainer() of the Java class Pod that enables agents to create an LDP container in your pod. You can check whether a container is already present in order not to create it again, which would cause additional containers that you do not need to be created.

        TIP: Creating containers based on the Solid Community Server documentation
        TIP: You should create a container as an empty resource, whose URL ends with "/".
        TIP: You can optionally check that your container is not present before creating the container.
     */

  /**
   * CArtAgO operation for creating a Linked Data Platform container in the Solid pod
   *
   * @param containerName The name of the container to be created
   *
   */
    @OPERATION
    public void createContainer(String containerName) {
        // log("1. Implement the method createContainer()");

        HttpClient client = HttpClient.newHttpClient();

        if (!containerName.endsWith("/")) {
            containerName += "/";
        }

        if (!podURL.endsWith("/")) {
            podURL += "/";
        }

        String containerURL = podURL + containerName;

        // Check if container exists
        try {
            HttpRequest headRequest = HttpRequest.newBuilder()
                    .uri(URI.create(containerURL))
                    .method("HEAD", HttpRequest.BodyPublishers.noBody())
                    .build();

            HttpResponse<Void> headResponse = client.send(headRequest, HttpResponse.BodyHandlers.discarding()); // No need to read the body

            if (headResponse.statusCode() == 200) {
                log("The container already exists: " + containerURL);
                return; // Exit early if the container exists
            }

        } catch (Exception e) {
            log("Container not found or error during HEAD request. Proceeding to create container: " + containerName);
        }

        // If container does not exist yet, it's time to create it
        try {
            HttpRequest putRequest = HttpRequest.newBuilder()
                    .uri(URI.create(containerURL)) // Reuse containerURL
                    .PUT(HttpRequest.BodyPublishers.noBody())
                    .build();

            HttpResponse<String> putResponse = client.send(putRequest, HttpResponse.BodyHandlers.ofString());

            // Response checking
            int statusCode = putResponse.statusCode();
            if (statusCode == 201 || statusCode == 200) {
                log("Container created successfully at: " + containerURL);
            } else {
                log("Failed to create container. HTTP status: " + statusCode);
            }
        } catch (Exception e) {
            log("Error while creating container: " + e.getMessage());
        }
    }



    /*
        Task 2.2 Adding data to an LDP container
        Implement the method publishData() of the Java class Pod that enables agents to publish data (text/plain) to an LDP container in your pod.

        TIP: Creating resources (e.g. publishing data to a container) based on the Solid Community Server documentation
        TIP: use the provided method: createStringFromArray in Pod.
     */
  /**
   * CArtAgO operation for publishing data within a .txt file in a Linked Data Platform container of the Solid pod
   *
   * @param containerName The name of the container where the .txt file resource will be created
   * @param fileName The name of the .txt file resource to be created in the container
   * @param data An array of Object data that will be stored in the .txt file
   */
    @OPERATION
    public void publishData(String containerName, String fileName, Object[] data) {
        // log("2. Implement the method publishData()");


        try {
            if (!containerName.endsWith("/")) {
                containerName += "/";
            }
            if (!podURL.endsWith("/")) {
                podURL += "/";
            }

            String resourceURL = podURL + containerName + fileName;

            String content = createStringFromArray(data);

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(resourceURL))
                    .header("Content-Type", "text/plain")
                    .PUT(HttpRequest.BodyPublishers.ofString(content))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            int statusCode = response.statusCode();
            if (statusCode == 201 || statusCode == 200 || statusCode == 205) {
                log("Data published successfully at: " + resourceURL);
            } else {
                log("Failed to publish data. HTTP status: " + statusCode);
            }
        } catch (Exception e) {
            // Handle exceptions
            log("Error while publishing data to container: " + e.getMessage());
        }
    }


    /*
        Task 2.3. Reading data from an LDP container
        Implement the method readData() of the Java class Pod that enables agents to read data (text/plain) from an LDP container in your pod.

        TIP: Retrieving resources (e.g. reading data from a container) based on the Solid Community Server documentation
        TIP: use the provided method: createArrayFromString in Pod.
     */
  /**
   * CArtAgO operation for reading data of a .txt file in a Linked Data Platform container of the Solid pod
   *
   * @param containerName The name of the container where the .txt file resource is located
   * @param fileName The name of the .txt file resource that holds the data to be read
   * @param data An array whose elements are the data read from the .txt file
   */
    @OPERATION
    public void readData(String containerName, String fileName, OpFeedbackParam<Object[]> data) {
        data.set(readData(containerName, fileName));
    }

  /**
   * Method for reading data of a .txt file in a Linked Data Platform container of the Solid pod
   *
   * @param containerName The name of the container where the .txt file resource is located
   * @param fileName The name of the .txt file resource that holds the data to be read
   * @return An array whose elements are the data read from the .txt file
   */
    public Object[] readData(String containerName, String fileName) {
        try {
            if (!containerName.endsWith("/")) {
                containerName += "/";
            }

            if (!podURL.endsWith("/")) {
                podURL += "/";
            }

            String resourceURL = podURL + containerName + fileName;

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(resourceURL))
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            int statusCode = response.statusCode();
            if (statusCode == 200) {
                String content = response.body(); // Get the body of the response
                log("Data read successfully from: " + resourceURL);
                log("Content: " + content);
                return createArrayFromString(content);
            } else {
                log("Failed to read data. HTTP status: " + statusCode);
            }
        } catch (Exception e) {
            log("Error while reading data from container: " + e.getMessage());
        }

        return new Object[0];
    }

  /**
   * Method that converts an array of Object instances to a string,
   * e.g. the array ["one", 2, true] is converted to the string "one\n2\ntrue\n"
   *
   * @param array The array to be converted to a string
   * @return A string consisting of the string values of the array elements separated by "\n"
   */
    public static String createStringFromArray(Object[] array) {
        StringBuilder sb = new StringBuilder();
        for (Object obj : array) {
            sb.append(obj.toString()).append("\n");
        }
        return sb.toString();
    }

  /**
   * Method that converts a string to an array of Object instances computed by splitting the given string with delimiter "\n"
   * e.g. the string "one\n2\ntrue\n" is converted to the array ["one", "2", "true"]
   *
   * @param str The string to be converted to an array
   * @return An array consisting of string values that occur by splitting the string around "\n"
   */
    public static Object[] createArrayFromString(String str) {
        return str.split("\n");
    }


  /**
   * CArtAgO operation for updating data of a .txt file in a Linked Data Platform container of the Solid pod
   * The method reads the data currently stored in the .txt file and publishes in the file the old data along with new data
   *
   * @param containerName The name of the container where the .txt file resource is located
   * @param fileName The name of the .txt file resource that holds the data to be updated
   * @param data An array whose elements are the new data to be added in the .txt file
   */
    @OPERATION
    public void updateData(String containerName, String fileName, Object[] data) {
        Object[] oldData = readData(containerName, fileName);
        Object[] allData = new Object[oldData.length + data.length];
        System.arraycopy(oldData, 0, allData, 0, oldData.length);
        System.arraycopy(data, 0, allData, oldData.length, data.length);
        publishData(containerName, fileName, allData);
    }
}
