using System;
using System.Collections.Generic;
using System.Linq;
using System.Linq.Expressions;
using System.Threading.Tasks;

namespace WebApiObjetos.Models.Repositories.Interfaces
{
    public interface IRepository<TEntity>
    {
        Task<IEnumerable<TEntity>> GetAll();
        Task<TEntity> GetById(int id);
        Task<TEntity> Add(TEntity entity);
        Task Update(TEntity entity);
        Task Delete(TEntity entity);
        Task<bool> DeleteById(int id);
        Task<IList<TEntity>> FindBy(Expression<Func<TEntity, bool>> predicate);
        Task<bool> Any(Expression<Func<TEntity, bool>> predicate);
    }
}
