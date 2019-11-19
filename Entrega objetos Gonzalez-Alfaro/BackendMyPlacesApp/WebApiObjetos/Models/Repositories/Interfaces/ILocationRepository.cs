using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;
using WebApiObjetos.Models.Entities;

namespace WebApiObjetos.Models.Repositories.Interfaces
{
    public interface ILocationRepository : IRepository<Location>
    {
         Task<bool> deleteLocation(int userId, int LocationId);
    }
}
