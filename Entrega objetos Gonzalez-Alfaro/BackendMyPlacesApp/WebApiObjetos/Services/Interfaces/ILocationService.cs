﻿using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;
using WebApiObjetos.Domain;

namespace WebApiObjetos.Services.Interfaces
{
    public interface ILocationService
    {
        Task<List<LocationDTO>> GetLocations(int userId);

        Task<List<LocationDTO>> GetLocationsById(List<String> locations);

        Task<LocationDTO> GetLocation(int userId, int locationId);

        Task<LocationDTO> AddLocation(LocationDTO location);

        Task<List<LocationDTO>> Buscar(LocationDTO location);

        Task<bool> DeleteLocation(int userId, int locationId);

        Task<bool> UpdateLocation(LocationDTO location);

        Task<ImageDTO> AddImage(ImageDTO image);

        Task<ImageDTO> GetImage(int imageId);

        Task<List<LocationDTO>> getLocationsInArea(string coordinates, int userId);

        Task<bool> ContactInformador(int userId, int imageId);

    }
}
