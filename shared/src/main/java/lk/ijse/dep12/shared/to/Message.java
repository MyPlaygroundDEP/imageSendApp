package lk.ijse.dep12.shared.to;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Message implements Serializable {
private byte[] imageData ;

}
