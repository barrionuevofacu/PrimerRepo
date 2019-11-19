using Microsoft.EntityFrameworkCore;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;
using WebApiObjetos.Models.Entities;
using WebApiObjetos.Models.Repositories.Interfaces;

namespace WebApiObjetos.Models.Repositories
{
    public class LocationRepository : RepositoryBase<Location> , ILocationRepository
    {
        public LocationRepository(DbContext dbContext) : base(dbContext){}

        public async Task<bool> deleteLocation(int userId, int LocationId)
        {
            var locationTodelete = await FindBy(x => x.Id.Equals(LocationId) && x.UserId.Equals(userId));
            if (locationTodelete.Count > 0)
            {
                await Delete(locationTodelete.First());
                return true;
            }
            return false;
        }
    }
}
