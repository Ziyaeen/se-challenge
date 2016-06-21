package waveBoot.exception;

import java.lang.Exception;

public class WaveBootUploadFileException extends Exception
{
      //Parameterless Constructor
      public WaveBootUploadFileException() {}

      //Constructor that accepts a message
      public WaveBootUploadFileException(String message)
      {
         super(message);
      }

}
