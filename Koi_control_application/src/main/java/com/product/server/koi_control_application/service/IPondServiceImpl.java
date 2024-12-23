package com.product.server.koi_control_application.service;

import com.product.server.koi_control_application.customException.AlreadyExistedException;
import com.product.server.koi_control_application.customException.NotFoundException;
import com.product.server.koi_control_application.model.Pond;
import com.product.server.koi_control_application.model.Users;
import com.product.server.koi_control_application.model.WaterQualityStandard;
import com.product.server.koi_control_application.repository.KoiFishRepository;
import com.product.server.koi_control_application.repository.PondRepository;
import com.product.server.koi_control_application.repository.UsersRepository;
import com.product.server.koi_control_application.serviceInterface.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;


@Service
@RequiredArgsConstructor
public class IPondServiceImpl implements IPondService {
    private final PondRepository pondRepository;
    private final UsersRepository usersRepository;
    private final IKoiFishService iKoiFishService;
    private final IWaterParameterService iWaterParameterService;
    private final IImageService iImageService;
    private final KoiFishRepository koiFishRepository;
    private final IPackageService iPackageService;
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Pond addPond(Pond pond) {
        Users user = usersRepository.findById(pond.getUserId()).orElseThrow(() -> new NotFoundException("User not found."));
//        if(!usersRepository.existsById(pond.getUserId()))
//            throw new NotFoundException("User not found.");

        if(pondRepository.existsByNameAndUserId(pond.getName(), pond.getUserId())){
            throw new AlreadyExistedException("Pond name existed.");
        }

        if (iPackageService.checkPondLimit(pond.getUserId(), user.getAUserPackage())){
            throw new NotFoundException("User package limit exceeded.");
        }

        Pond saved = pondRepository.save(pond);
        WaterQualityStandard  waterQualityStandard = new WaterQualityStandard();
        waterQualityStandard.setPondId(pond.getId());
        waterQualityStandard.calculateValues(pond.getVolume(),koiFishRepository.findAllByPondId(pond.getId()));
        iWaterParameterService.saveWaterQualityStandard(waterQualityStandard);

        return getPond(saved.getId());

    }

    @Override
    public Pond getPond(int id) {
         Pond pond1 = pondRepository.findById(id).orElseThrow(() -> new NotFoundException("Pond not found"));
        pond1.setFishCount(iKoiFishService.countKoiFishByPondId(id));
        pondRepository.save(pond1);
         return pond1;
    }

    @Override
    public Page<Pond> getPonds(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Pond> ponds = pondRepository.findAll(pageable);
        ponds.forEach(pond -> pond.setFishCount(iKoiFishService.countKoiFishByPondId(pond.getId())));
        pondRepository.saveAll(ponds.getContent());
        return ponds;
    }


    //Theêm bởi trung
    @Override
    public List<Pond> getPonds() {
        return pondRepository.findAll() ;
    }

    @Override
    public Page<Pond> getAllPondByUserId(int userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Pond> ponds = pondRepository.findAllByUserId(userId, pageable);
        ponds.forEach(pond -> pond.setFishCount(iKoiFishService.countKoiFishByPondId(pond.getId())));
        pondRepository.saveAll(ponds.getContent());
        return ponds;
    }


    //Theêm bởi trung
    @Override
    public List<Pond> getAllPondByUserId(int userId) {
        return pondRepository.findAllPondByUserId(userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deletePond(int id) {
        Pond pond = getPond(id);
        pondRepository.delete(pond);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Pond updatePond(int id, Pond request, MultipartFile file) throws IOException {
        Pond pond = getPond(id);
        if(!usersRepository.existsById(request.getUserId()))
            throw new NotFoundException("User not found.");
        if(pondRepository.existsByNameAndUserIdExceptId(request.getName(), request.getUserId(), id))
            throw new AlreadyExistedException("Pond name existed.");


        if(file != null && !file.isEmpty()){
            String filename = iImageService.updateImage(pond.getImageUrl(), file);
            pond.setImageUrl(filename);
        }else{
            pond.setImageUrl(pond.getImageUrl());
        }


        pond.setName(request.getName());
        pond.setWidth(request.getWidth());
        pond.setLength(request.getLength());
        pond.setDepth(request.getDepth());
        pond.setFishCount(iKoiFishService.countKoiFishByPondId(id));

        WaterQualityStandard waterQualityStandard = iWaterParameterService.getWaterQualityByPondId(pond.getId());
        waterQualityStandard.calculateValues(pond.getVolume(),koiFishRepository.findAllByPondId(pond.getId()));
        iWaterParameterService.saveWaterQualityStandard(waterQualityStandard);

        return pondRepository.save(pond);
    }
}


