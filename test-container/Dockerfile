FROM openjdk:11-slim

# Create a simple directory structure
WORKDIR /app

# Create a simple Java file with crypto usage
RUN echo 'import javax.crypto.Cipher; public class Test { public void test() throws Exception { Cipher c = Cipher.getInstance("AES/GCM/NoPadding"); } }' > Test.java

# Compile it
RUN javac Test.java

# Package it into a JAR
RUN jar cf test.jar Test.class

# Create a simple directory structure
RUN mkdir -p /app/lib
RUN cp test.jar /app/lib/

# Set a simple entrypoint
CMD ["java", "-version"]
