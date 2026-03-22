package dto;

import java.util.List;

 //The reports about how the loading went and the errors if there were any
 public record LoadReport(
         boolean ok,
         List<Exception> errors
 ) {}