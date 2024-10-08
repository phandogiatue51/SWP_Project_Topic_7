package com.product.server.koi_control_application.serviceInterface;

import com.product.server.koi_control_application.model.Pond;
import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface IPondService {
    Pond addPond(Pond pond);
    Pond getPond(int id);
    Page<Pond> getPonds(int page, int size);

    //Theêm bởi trung
    List<Pond> getPonds();
    Page<Pond> getAllPondByUserId(int userId, int page, int size);

    //Theêm bởi trung
    List<Pond> getAllPondByUserId(int userId);
    void deletePond(int id);

    Pond updatePond(int id, Pond request, MultipartFile file) throws IOException;
}
