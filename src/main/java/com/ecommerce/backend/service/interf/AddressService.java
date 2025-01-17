package com.ecommerce.backend.service.interf;

import com.ecommerce.backend.dto.AddressDto;
import com.ecommerce.backend.dto.Response;

public interface AddressService {
    Response saveAndUpdateAddress(AddressDto addressDto);
}
